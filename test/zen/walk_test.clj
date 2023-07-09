(ns zen.walk-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core]
   [zen.walk :as sut]))


(def rest-ns
  '{:ns rest

    rpc
    {:zen/tags #{zen/tag zen/schema}
     :type zen/map
     :keys {:params {:confirms #{zen/schema}}
            :result {:confirms #{zen/schema}}}}

    op-engine
    {:zen/tags #{zen/tag}
     :zen/desc "tag for op engines"}

    op
    {:zen/tags   #{zen/tag zen/schema}
     :type       zen/map
     :require    #{:engine}
     :schema-key {:key :engine}
     :keys       {:engine {:type zen/symbol :tags #{op-engine}}}}

    api-op
    {:zen/tags #{zen/schema}
     :type zen/case
     :case [{:when {:type zen/symbol} :then {:type zen/symbol :tags #{op}}}
            {:when {:type zen/map} :then {:type zen/map
                                          :confirms #{op}}}]}

    route-node
    {:zen/tags #{zen/schema}
     :type zen/map
     :keys {:apis    {:type zen/set
                      :every {:type zen/symbol :tags #{api}}}
            :routes  {:type zen/map
                      :key {:type zen/case
                            :case [{:when {:type zen/string}
                                    :then {}}
                                   {:when {:type zen/vector}
                                    :then {:type zen/vector
                                           :every {:type zen/keyword}
                                           :minItems 1
                                           :maxItems 1}}]}
                      :values {:confirms #{route-node}}}
            :methods {:type zen/map
                      :keys {:GET    {:confirms #{api-op}}
                             :POST   {:confirms #{api-op}}
                             :PUT    {:confirms #{api-op}}
                             :DELETE {:confirms #{api-op}}
                             :PATCH  {:confirms #{api-op}}}}}}

    api
    {:zen/tags #{zen/tag zen/schema}
     :type zen/map
     :keys {:routing {:confirms #{route-node}}}}})


(t/deftest sch-seq-test
  (t/testing "schema traversing"
    (def ztx (zen.core/new-context))
    (zen.core/load-ns ztx '{:ns myns
                            foo {:zen/tags #{zen/schema}
                                 :type     zen/map
                                 :require  #{:a}
                                 :keys     {:c {:type zen/string}}}

                            mysch
                            {:zen/tags #{zen/schema}
                             :type zen/map
                             :confirms #{myns/foo}
                             :zen/desc "2"
                             :keys {:a {:type zen/string
                                        :zen/desc "3"}
                                    :b {:type zen/vector
                                        :zen/desc "4"
                                        :every {:type zen/map
                                                :zen/desc "5"
                                                :require #{:c}
                                                :keys {:c {:zen/desc "6"
                                                           :type zen/any}}}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
     (sut/zen-dsl-leafs-seq ztx (zen.core/get-symbol ztx 'myns/mysch))
      [{:path [:confirms]                          :value #{'myns/foo}}
       {:path [:keys :a :type]                     :value 'zen/string}
       {:path [:keys :a :zen/desc]                 :value "3"}
       {:path [:keys :b :every :keys :c :type]     :value 'zen/any}
       {:path [:keys :b :every :keys :c :zen/desc] :value "6"}
       {:path [:keys :b :every :require]           :value #{:c}}
       {:path [:keys :b :every :type]              :value 'zen/map}
       {:path [:keys :b :every :zen/desc]          :value "5"}
       {:path [:keys :b :type]                     :value 'zen/vector}
       {:path [:keys :b :zen/desc]                 :value "4"}
       #_{:path [:require]                           :value #{:a}}
       {:path [:type]                              :value 'zen/map}
       {:path [:zen/desc]                          :value "2"}
       {:path [:zen/name]                          :value 'myns/mysch}
       {:path [:zen/tags]                          :value #{'zen/schema}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil]))

  (t/testing "rpc method dsl"
    (zen.core/load-ns ztx rest-ns)
    (zen.core/load-ns ztx '{:ns myns3
                            :import #{rest}

                            myrpc
                            {:zen/tags #{rest/rpc}
                             :params {:type zen/map
                                      :require #{:foo}
                                      :keys {:foo {:type zen/string}}}
                             :result {:type zen/map
                                      :require #{:bar}
                                      :keys {:bar {:type zen/string}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
     (sut/zen-dsl-leafs-seq ztx (zen.core/get-symbol ztx 'myns3/myrpc))
      [{:path [:params :keys :foo :type nil] :value 'zen/string}
       {:path [:params :require nil]         :value #{:foo}}
       {:path [:params :type nil]            :value 'zen/map}
       {:path [:result :keys :bar :type]     :value 'zen/string}
       {:path [:result :require nil]         :value #{:bar}}
       {:path [:result :type nil]            :value 'zen/map}
       {:path [:zen/name nil]                :value 'myns3/myrpc}
       {:path [:zen/tags nil]                :value #{'rest/rpc}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil]))

  (t/testing "rest api dsl"
    (zen.core/load-ns ztx rest-ns)
    (zen.core/load-ns ztx '{:ns myns2
                            :import #{rest}

                            engine
                            {:zen/tags #{rest/op-engine zen/schema}}

                            op
                            {:zen/tags #{rest/op}
                             :engine engine}

                            other-api
                            {:zen/tags #{rest/api}
                             :routing {:methods {:DELETE op}}}

                            myapi
                            {:zen/tags #{rest/api}
                             :routing {:apis    #{other-api}
                                       :methods {:GET op}
                                       :routes  {"$export" {:methods {:POST op}}
                                                 [:id]     {:methods {:GET op}}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
     (sut/zen-dsl-leafs-seq ztx (zen.core/get-symbol ztx 'myns2/myapi))
      [#_{:path [:DELETE nil] :value 'myns2/delete}
       {:path [:routing :apis nil]                            :value #{'myns2/other-api}}
       {:path [:routing :methods :GET nil]                    :value 'myns2/op}
       {:path [:routing :routes "$export" :methods :POST nil] :value 'myns2/op}
       {:path [:routing :routes [:id] :methods :GET nil]      :value 'myns2/op}
       {:path [:zen/name]                                     :value 'myns2/myapi}
       {:path [:zen/tags]                                     :value #{'rest/api}} #_"NOTE: zen itself, not schema. move? Same for :zen/name"
       nil]))

  (t/testing "multiple tags"
    (zen.core/load-ns ztx '{:ns myns4

                            mytag
                            {:zen/tags #{zen/schema zen/tag}
                             :type zen/map
                             :keys {:foo {:const {:value :bar}}}}

                            mysym
                            {:zen/tags #{zen/schema mytag}
                             :foo :bar
                             :type zen/string}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
     (sut/zen-dsl-leafs-seq ztx (zen.core/get-symbol ztx 'myns4/mysym))
      [{:path [:foo nil]       :value :bar}
       {:path [:type nil]      :value 'zen/string}
       {:path [:zen/name nil]  :value 'myns4/mysym}
       {:path [:zen/tags nil]  :value '#{zen/schema myns4/mytag}}
       nil])))


(t/deftest custom-compile-target-test
  (t/testing "attribute generation"
    (def ztx (zen.core/new-context {}))

    (def my-structs-ns
      '{:ns my-sturcts

        reference
        {:zen/tags #{zen/schema}
         :type zen/map
         :keys {:id {:type zen/string}
                :resourceType {:type zen/string}}}

        name
        {:zen/tags #{zen/schema}
         :type zen/map
         :keys {:given {:type zen/vector
                        :every {:type zen/string}}
                :family {:type zen/string}}}

        User
        {:zen/tags #{zen/schema}
         :type zen/map
         :keys {:id {:type zen/string}
                :email {:type zen/string
                        #_#_:regex "@"}
                :deceased {:fhir/polymorphic true
                           :type zen/map
                           :keys {:boolean {:type zen/boolean}
                                  :dateTime {:type zen/datetime}}}
                :name {:type zen/vector
                       :every {:confirms #{name}}}
                :link {:type zen/vector
                       :every {:type zen/map
                               :keys {:other {:confirms #{reference}}}}}}}})

    (zen.core/load-ns ztx my-structs-ns)


    (defn schema-key->attribute-dispatch [acc ztx wtx k v path] k)


    (defmulti schema-key->attribute #'schema-key->attribute-dispatch
      :default ::default)


    (defmethod schema-key->attribute ::default [acc ztx wtx k v path]
      nil)


    (defn mk-attr-path [wtx]
      (->> (:data-path wtx)
           (remove #(= :# %))
           (mapv name)))


    (defn mk-attr-id [wtx]
      (let [path (mk-attr-path wtx)
            entity (:schema-sym wtx)]
        (keyword
          (str (name entity)
               (when-let [pth (seq path)]
                 (str "." (clojure.string/join "." pth)))))))


    (defmethod schema-key->attribute :type [acc ztx wtx k v path]
      {:attr
       (let [tp (keyword (name v))]
         (if (= :vector tp)
           {:isCollection true}
           {:type (keyword (name v))}))})


    (defmethod schema-key->attribute :confirms [acc ztx wtx k v path]
      {:attr {:type (keyword (name (first v)))}})


    (defmethod schema-key->attribute :fhir/polymorphic [acc ztx wtx _k v path]
      (when (true? v)
        {:attr
         {:types (->> (vals (get-in wtx [:schema :keys]))
                      (mapv :type)
                      (mapv name)
                      (mapv keyword)
                      set)}
         :skip-polymorpic-keys (keys (get-in wtx [:schema :keys]))}))


    (defn make-attribute [acc ztx {:as wtx :keys [k v]}]
      (or (when-let [path (not-empty (mk-attr-path wtx))]
            (when-let [{:keys [attr]}
                       (schema-key->attribute acc ztx wtx k v path)]
              (-> acc
                  (update-in [:attrs (mk-attr-id wtx)]
                             merge {:path path} attr))))
          acc))


    (def r
      (sut/reduce-schema-seq-kv
        #(make-attribute %1 ztx %2)
        {:attrs {}}
        (sut/schema-seq ztx (zen.core/get-symbol ztx 'my-sturcts/User))))


    (t/is (= {:User.id         {:path ["id"] :type :string}
              :User.email      {:path ["email"] :type :string}
              :User.name       {:path ["name"] :isCollection true :type :name}
              :User.link       {:path ["link"] :type :map :isCollection true}
              :User.link.other {:path ["link" "other"] :type :reference}
              :User.deceased   {:path ["deceased"]
                                :type :map #_"FIXME"
                                :types #{:boolean :datetime}}

              :reference.id           {:path ["id"] :type :string}
              :reference.resourceType {:path ["resourceType"] :type :string}

              :name.given  {:path ["given"] :type :string :isCollection true}
              :name.family {:path ["family"] :type :string}


              #_"FIXME: should skip polymorphic keys & type"
              :User.deceased.dateTime {:path ["deceased" "dateTime"], :type :datetime}
              :User.deceased.boolean {:path ["deceased" "boolean"], :type :boolean}}
             (:attrs r)))))


(t/deftest schema-seq-test
  (def ztx (zen.core/new-context {}))

  (def myns
    '{:ns myns

      baz
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:quux {:type zen/string}}}

      bar
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:baz {:confirms #{baz}}}}

      foo
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:bar {:confirms #{bar}}
              :baz {:confirms #{baz}}}}})

  (zen.core/load-ns ztx myns)

  (matcho/match
    (sut/schema-bf-seq ztx (zen.core/get-symbol ztx 'myns/foo))
    '[{:schema-sym myns/foo :schema-stack [myns/foo nil] :full-data-path [nil]      :schema {:zen/name myns/foo}}
      {:schema-sym myns/foo :schema-stack [myns/foo nil] :full-data-path [:bar nil] :schema {:confirms #{myns/bar}}}
      {:schema-sym myns/foo :schema-stack [myns/foo nil] :full-data-path [:baz nil] :schema {:confirms #{myns/baz}}}

      {:schema-sym myns/bar :schema-stack [myns/foo myns/bar nil] :full-data-path [:bar nil]       :schema {:zen/name myns/bar}}
      {:schema-sym myns/baz :schema-stack [myns/foo myns/baz nil] :full-data-path [:baz nil]       :schema {:zen/name myns/baz}}
      {:schema-sym myns/bar :schema-stack [myns/foo myns/bar nil] :full-data-path [:bar :baz nil]  :schema {:confirms #{myns/baz}}}
      {:schema-sym myns/baz :schema-stack [myns/foo myns/baz nil] :full-data-path [:baz :quux nil] :schema {:type zen/string}}

      {:schema-sym myns/baz :schema-stack [myns/foo myns/bar myns/baz nil] :full-data-path [:bar :baz nil]       :schema {:zen/name myns/baz}}
      {:schema-sym myns/baz :schema-stack [myns/foo myns/bar myns/baz nil] :full-data-path [:bar :baz :quux nil] :schema {:type zen/string}}
      nil])

  (matcho/match
    (sut/schema-seq ztx (zen.core/get-symbol ztx 'myns/foo))
    '[{:schema-sym myns/foo :schema-stack [myns/foo] :full-data-path [] :schema {:zen/name myns/foo}}
      {:schema-sym myns/foo :schema-stack [myns/foo] :full-data-path [:bar] :schema {:confirms #{myns/bar}}}
      {:schema {:zen/name myns/bar}}
      {:schema {:confirms #{myns/baz}} :schema-stack [myns/foo myns/bar nil]}
      {:schema {:zen/name myns/baz}}
      {:schema {:type zen/string}}
      {:schema {:confirms #{myns/baz}} :schema-stack [myns/foo nil]}
      {:schema {:zen/name myns/baz}}
      {:schema {:type zen/string}}
      nil]))


(t/deftest superschema-test
  (def ztx (zen.core/new-context {}))

  (def myns
    '{:ns myns

      baz
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:quux {:type zen/string}}}

      bar
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:baz {:confirms #{baz}}}}

      foo
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:bar {:confirms #{bar}}}}})

  (zen.core/load-ns ztx myns)

  (matcho/match
    (sut/reduce-schema-seq-kv
      (fn [acc {:keys [k v full-schema-path]}]
        (if (or (= :confirms k)
                (and (contains? #{:zen/tags :zen/name} k)
                     (< 1 (count full-schema-path))))
          acc
          (assoc-in acc
                    (->> (conj full-schema-path k)
                         (remove symbol?)
                         (remove #(= :confirms %)))
                    v)))
      {}
      (sut/schema-bf-seq ztx (zen.core/get-symbol ztx 'myns/foo)))
    {:zen/tags #{'zen/schema}
     :keys {:bar {:keys {:baz {:keys {:quux {:type 'zen/string}}}}}}}))


(t/deftest walk-test
  (def ztx (zen.core/new-context {}))

  (def my-structs-ns
    '{:ns my-sturcts

      User
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:id {:type zen/string}
              :email {:type zen/string
                      #_#_:regex "@"}
              :name {:type zen/vector
                     :every {:type zen/map
                             :keys {:given {:type zen/vector
                                            :every {:type zen/string}}
                                    :family {:type zen/string}}}}}}})

  (zen.core/load-ns ztx my-structs-ns)

  (def ts-typedef-assert
    (str "type User = {\n"
         "  id: string;\n"
         "  email: string;\n"
         "  name: Array < {\n"
         "    given: Array < string >;\n"
         "    family: string\n"
         "  } >\n"
         "}"))

  (def r
    (sut/postwalk-schemas
      nil
      (fn [schema]
        (str
          (when-let [nm (:zen/name schema)]
            (str "type " (name nm) " = "))
          (case (:type schema)
            zen/map    (str "{\n"
                            (clojure.string/join
                              ";\n"
                              (map
                                (fn [[k v]]
                                  (str "  " (name k) ": "
                                       (->> (clojure.string/split-lines v)
                                            (clojure.string/join "\n  "))))
                                (:keys schema)))
                            "\n}")
            zen/vector (str "Array < " (:every schema) " >")
            (name (:type schema)))))
      (zen.core/get-symbol ztx 'my-sturcts/User)))

  (t/is (= ts-typedef-assert r)))

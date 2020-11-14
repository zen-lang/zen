(ns zen.validation-test
  (:require [zen.core]
            [matcho.core :as matcho]
            [clojure.test :refer [deftest is testing]]))


(def tctx (zen.core/new-context {:unsafe true}))

(zen.core/load-ns
 tctx {'ns 'myapp
       'str {:zen/tags #{'zen/schema}
             :type 'zen/string}

       'mykey {:zen/tags #{'zen/schema 'zen/property}
               :type 'zen/string}

       'path {:zen/tags #{'zen/schema}
              :type 'zen/vector
              :every {:type 'zen/case
                      :case [{:when {:type 'zen/string}}
                             {:when {:type 'zen/map}
                              :then {:type 'zen/map :require #{:name} :keys {:name {:type 'zen/string}}}}]}}

       'maps-case {:zen/tags #{'zen/schema}
                   :type 'zen/case
                   :case [{:when {:type 'zen/map
                                  :require #{:person}
                                  :keys {:person {:type 'zen/boolean}}}
                           :then {:type 'zen/map
                                  :require #{:name}
                                  :keys {:name {:type 'zen/string}}}}
                          {:when {:type 'zen/map
                                  :require #{:org}
                                  :keys {:org {:type 'zen/boolean}}}
                           :then {:type 'zen/map
                                  :require #{:title}
                                  :keys {:title {:type 'zen/string}}}}]}

       'const {:zen/tags #{'zen/schema}
               :type 'zen/string
               :const {:value "fixed"}}

       'const-map {:zen/tags #{'zen/schema}
                   :type 'zen/map
                   :keys {:fixed {:type 'zen/string}}
                   :const {:value {:fixed "fixed"}}}

       'Address {:zen/tags #{'zen/schema}
                 :type 'zen/map
                 :require #{:city}
                 :keys {:city {:type 'zen/string}
                        :line {:type 'zen/vector
                               :every {:type 'zen/string}}}}

       'Identifier {:zen/tags #{'zen/schema}
                    :type 'zen/map
                    :keys {:value {:type 'zen/string}
                           :system {:type 'zen/string}}}

       'User {:zen/tags #{'zen/schema}
              :type 'zen/map
              :keys {:id   {:type 'zen/string}
                     :name {:type 'zen/string :minLength 3}
                     :address {:confirms #{'Address}
                               :type 'zen/map
                               :keys {:home-number {:type 'zen/string}}}
                     :identifiers {:type 'zen/vector
                                   :minItems 2
                                   :every {:confirms #{'Identifier}
                                           :type 'zen/map
                                           :keys {:extra {:type 'zen/string}}
                                           :require #{:system}}}}
              :require #{:name}}

       'Contactable {:zen/tags #{'zen/schema}
                     :type 'zen/map
                     :keys {:contact {:type 'zen/map
                                      :keys {:phone {:type 'zen/string}
                                             :ex    {:type 'zen/string}}}}}

       'SuperUser {:zen/tags #{'zen/schema}
                   :confirms #{'User 'Contactable}
                   :type 'zen/map
                   :keys {:role {:type 'zen/string}
                          :contact {:type 'zen/map
                                    :require [:ex]}}}

       'Settings {:zen/tags #{'zen/schema}
                  :type 'zen/map
                  :keys {:headers {:type 'zen/map
                                   :keys {:content-type {:type 'zen/string :minLength 3}}
                                   :values {:type 'zen/string}}}}
       'email {:zen/tags #{'zen/schema}
               :type 'zen/string
               :regex "^.*@.*$"}
       })

;; (get-in @tctx [:syms 'myapp/User])

(defmacro vmatch [schemas subj res]
  `(let [res# (zen.core/validate tctx ~schemas ~subj)]
     (matcho/match res# ~res)
     res#))

(defmacro match [schema subj res]
  `(let [res# (zen.core/validate tctx #{~schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))

(defmacro valid [schema subj]
  `(let [res# (zen.core/validate tctx #{~schema} ~subj)]
     (is (empty? (:errors res#)))
     (:errors res#)))

(defmacro valid-schema! [subj]
  `(let [res# (zen.core/validate tctx #{'zen/schema} ~subj)]
     (is (empty? (:errors res#)))
     res#))

(defmacro invalid-schema [subj res]
  `(let [res# (zen.core/validate tctx #{'zen/schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))


(deftest test-validation


  (is (empty? (:errors @tctx)))

  (vmatch #{'myapp/str} 1
          {:errors [{:message #"Expected type of 'string"}]})

  (vmatch #{'myapp/str} "string"
          {:errors empty?})

  (vmatch #{'myapp/User} 1
          {:errors [{:message #"Expected type of 'map"}]})

  (vmatch #{'myapp/User} {:name 1}
          {:errors [{:message #"Expected type of 'string"
                     :path [:name]
                     :schema ['myapp/User :name]}
                    nil?]})

  (vmatch #{'myapp/User} {:name "niquola"}
          {:errors empty?})

  (vmatch #{'myapp/User} {:id "niquola"}
          {:errors [{:message #":name is required"
                     :path [:name]
                     :schema ['myapp/User :require]}]})

  (vmatch #{'myapp/User} {:name "niquola" :role "niquola"}
          {:errors [{:path [:role]
                     :type "unknown-key"
                     :message "unknown key :role"}]})

  (vmatch #{'myapp/User} {:name "niquola" :address {:extra "niquola"}}
          {:errors [{:path [:address :city]
                     :schema ['myapp/User :address 'myapp/Address :require]
                     :type "require"
                     :message ":city is required"}
                    {:path [:address :extra]
                     :type "unknown-key"
                     :message "unknown key :extra"}]})

  (vmatch #{'myapp/User} {:name "niquola" :contact {:phone "888"}}
          {:errors
           [{:type "unknown-key",
             :message "unknown key :contact",
             :path [:contact]}]})

  (vmatch #{'myapp/SuperUser} {:name "niquola" :role "niquola"}
          {:errors empty?})

  (vmatch #{'myapp/SuperUser} {:name "niquola" :contact {:phone "888"}}
          {:errors
           [{:message ":ex is required",
             :type "require",
             :path [:contact :ex],
             :schema ['myapp/SuperUser :contact :require]}
            nil?]})

  (vmatch #{'myapp/User} {:name "niquola" :address {:line "ups" :city "city"}}
          {:errors
           [{:message #"Expected type of 'vector",
             :type "type",
             :path [:address :line],
             :schema ['myapp/User :address 'myapp/Address :line]}
            nil?]})

  (match 'myapp/User
         {:name "niquola" :address {:line ["a" "b" 1] :city "city"}}
         [{:message "Expected type of 'string, got 'long",
           :type "string.type",
           :path [:address :line 2],
           :schema ['myapp/User :address 'myapp/Address :line :every]}
          nil?])

  (vmatch #{'myapp/User} {:name "niquola" :identifiers [1 {:ups "ups"}]}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:identifiers 0],
             :schema ['myapp/User :identifiers :every 'myapp/Identifier]}
            {:message "Expected type of 'map, got 1",
             :type "type",
             :path [:identifiers 0],
             :schema ['myapp/User :identifiers :every]}
            {:message ":system is required",
             :type "require",
             :path [:identifiers 1 :system],
             :schema ['myapp/User :identifiers :every :require]}
            {:type "unknown-key",
             :message "unknown key :ups",
             :path [:identifiers 1 :ups]}]})

  (vmatch #{'myapp/User} {:name "niquola" :identifiers [{:system "s1" :value "v1" :extra "value"}
                                                        {:system "s1" :value "v2"}]}
          {:errors empty?})

  (vmatch #{'myapp/User}
          {:name "niquola"
           :myapp/unexisting "ups"
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          {:errors
           [{:type "unknown-key",
             :message "unknown key :myapp/unexisting",
             :path [:myapp/unexisting]}]})

  (vmatch #{'myapp/User}
          {:name "niquola"
           :myapp/mykey "hi"
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          {:errors empty?})

  (match 'myapp/User
          {:name "niquola"
           :myapp/mykey 1
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          [{:message "Expected type of 'string, got 'long",
            :type "string.type",
            :path [:myapp/mykey],
            :schema ['myapp/User :myapp/mykey]}])

  (vmatch #{'myapp/User} {:name "niquola" :identifiers [{:system "s1" :value "v1" :extra "value"}]}
          {:errors [{:message "Expected >= 2, got 1" :path [:identifiers] :schema ['myapp/User :identifiers :minItems]}]})

  (vmatch #{'zen/schema} {:type 'zen/string}
          {:errors empty?})

  (vmatch #{'zen/schema} {:type 'zen/map :keys 1}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:keys],
             :schema ['zen/schema :schema-key 'zen/map :keys]}]})

  (vmatch #{'myapp/Settings} {:headers {:a "str"}}
          {:errors empty?})

  (vmatch #{'myapp/Settings} {:headers {:a 1}}
          {:errors [{:path [:headers :a]
                     :schema ['myapp/Settings :headers :values]
                     :message #"Expected type of 'string"}]})

  (match 'myapp/Settings
          {:headers {:content-type "up" :a "up"}}
          [{:message "Expected length >= 3, got 2",
            :type "string.minLength",
            :path [:headers :content-type],
            :schema ['myapp/Settings :headers :content-type :minLength]}])

  (vmatch #{'zen/schema} {:type 'zen/map :keys {:prop 1}}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:keys :prop],
             :schema ['zen/schema :schema-key 'zen/map :keys :values]}]})

  (vmatch #{'zen/schema} {:type 'zen/map
                          :keys {:ups {:type 'zen/string
                                       :minLength "ups"}}}
          {:errors
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [:keys :ups :minLength],
             :schema
             ['zen/schema :schema-key 'zen/map :keys :values 'zen/schema :schema-key 'zen/string :minLength]}]})

  (vmatch #{'zen/schema}
          {:type 'zen/map
           :keys {:minLength {:type 'zen/integer, :min 0}
                  :maxLength {:type 'zen/integer, :min 0}
                  :regex {:type 'zen/regex}}}
          {:errors empty?})

  (vmatch #{'zen/schema}
          {:type 'zen/map
           :keys {:minLength {:type 'zen/integer, :ups 0}
                  :maxLength {:type 'zen/integer, :min ""}
                  :regex {:type 'zen/regex}}}

          {:errors
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [:keys :maxLength :min],
             :schema ['zen/schema :schema-key 'zen/map :keys :values 'zen/schema :schema-key 'zen/integer :min]}
            {:type "unknown-key",
             :message "unknown key :ups",
             :path [:keys :minLength :ups]}]})

  (match 'myapp/email
          "ups"
          [{:message "Expected match /^.*@.*$/, got \"ups\"",
            :type "string.regex",
            :path [],
            :schema ['myapp/email :regex]}])

  (vmatch #{'zen/schema}
          {:type 'zen/case
           :case [{:when {:type 'zen/integer}
                   :then {:type 'zen/integer}}
                  {:when {:type 'zen/map}
                   :then {:type 'zen/map :keys {:name {:type 'zen/string}}}}]}
          {:errors empty?})

  (vmatch #{'myapp/path}
          ["string" {:name "name"} "rest"]
          {:errors empty?})

  (vmatch #{'myapp/path}
          ["string" {:name "name"} 1 {:name "ups"}]
          {:errors
           [{:message
             "Expected one of ({:type zen/string} {:type zen/map}), but none is conformant",
             :type "case",
             :path [2],
             :schema ['myapp/path :every :case]}]})

  (vmatch #{'myapp/path}
          ["string" {:name "name"} 1 {:name "ups"}]
          {:errors
           [{:message
             "Expected one of ({:type zen/string} {:type zen/map}), but none is conformant",
             :type "case",
             :path [2],
             :schema ['myapp/path :every :case]}]})


  (vmatch #{'myapp/maps-case}
          {:person true :name "Ok"}
          {:errors empty?})

  (vmatch #{'myapp/maps-case}
          {:person true}
          {:errors
           [{:message ":name is required",
             :type "require",
             :path [:name],
             :schema ['myapp/maps-case :case 0 :then :require]}]})

  (vmatch #{'myapp/maps-case}
          {:org true}
          {:errors
           [{:message ":title is required",
             :type "require",
             :path [:title],
             :schema ['myapp/maps-case :case 1 :then :require]}]})

  (vmatch #{'myapp/maps-case}
          {:something true}
          {:errors
           [{:message
             "Expected one of ({:type zen/map, :require #{:person}, :keys {:person {:type zen/boolean}}} {:type zen/map, :require #{:org}, :keys {:org {:type zen/boolean}}}), but none is conformant",
             :type "case",
             :path [],
             :schema ['myapp/maps-case :case]}]})

  (vmatch #{'myapp/const} "ups"
          {:errors
           [{:message "Expected 'fixed', got 'ups'",
             :type "schema",
             :path [],
             :schema ['myapp/const]}]})

  (vmatch #{'myapp/const} "fixed"
          {:errors empty?})

  (vmatch #{'myapp/const} 1
          {:errors
           [{:message "Expected 'fixed', got '1'",
             :schema ['myapp/const]}
            {:message "Expected type of 'string, got 'long",
             :schema ['myapp/const]}]})

  (vmatch #{'myapp/const-map} {:fixed "fixed"}
          {:errors empty?})

  (vmatch #{'myapp/const-map} {:fixed "ups"}
          {:errors
           [{:message "Expected '{:fixed \"fixed\"}', got '{:fixed \"ups\"}'",
             :type "schema",
             :path [],
             :schema ['myapp/const-map]}]})

  ;; (vmatch #{'zen/schema}
  ;;         {:zen/tags #{'zen/schema}
  ;;          :type 'zen/string
  ;;          :const {:value 1}}
  ;;         {:errors [{}]})
  (testing "zen/string"

    (def str-sch {:zen/tags #{'zen/schema}
                  :type 'zen/string
                  :minLength 3
                  :maxLength 5
                  ;; :regex ".*@.*"
                  })

    (valid-schema! str-sch)

    (invalid-schema
     {:zen/tags #{'zen/schema}
      :type 'zen/string
      :minLength "a"
      :maxLength 2.1
      :regex {}}

     [{:message "Expected type of 'integer, got 'string",
       :type "primitive-type",
       :path [:minLength],
       :schema ['zen/schema :schema-key 'zen/string :minLength]}
      {:message "Expected type of 'integer, got 'double",
       :type "primitive-type",
       :path [:maxLength],
       :schema ['zen/schema :schema-key 'zen/string :maxLength]}
      {:message "Expected type of 'regex, got 'persistentarraymap",
       :type "primitive-type",
       :path [:regex],
       :schema ['zen/schema :schema-key 'zen/string :regex]}])


    (zen.core/load-ns! tctx {'ns 'test.str 'str str-sch})


    (valid 'test.str/str "a@a")

    )

  ;; tessting map
  (testing "zen/map"

    (valid-schema!
     {:zen/tags #{'zen/schema}
      :type 'zen/map
      :key {:type 'zen/string}})

    (zen.core/load-ns!
     tctx {'ns 'test.map
           'just-map {:zen/tags #{'zen/schema}
                      :type 'zen/map
                      :values {:type 'zen/any}}

           'str-keys {:zen/tags #{'zen/schema}
                      :type 'zen/map
                      :key {:type 'zen/string}}

           'int-keys {:zen/tags #{'zen/schema}
                      :type 'zen/map
                      :key {:type 'zen/integer}}})

    (valid 'test.map/just-map {:a 1})

    (match 'test.map/just-map 1
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :schema ['test.map/just-map]}])

    (valid 'test.map/str-keys {"a" 1 "b" 2})
    (match 'test.map/str-keys {:a 1 "b" 2}
           [{:message "Expected type of 'string, got 'keyword",
             :type "string.type",
             :path [:a],
             :schema ['test.map/str-keys :key]}])


    ;; schema-key

    (def sk-sch
      {'ns 'test.map.sk

       'pt {:zen/tags #{'zen/schema}
            :type 'zen/map
            :require #{:name}
            :keys {:name {:type 'zen/string}}}

       'org {:zen/tags #{'zen/schema}
             :type 'zen/map
             :require #{:title}
             :keys {:title {:type 'zen/string}}}

       'obj {:zen/tags #{'zen/schema}
             :type 'zen/map
             :schema-key {:key :kind :ns "test.map.sk"}
             :keys {:kind {:type 'zen/string}}}})

    (valid-schema! (get sk-sch 'obj))

    (zen.core/load-ns! tctx sk-sch)

    (valid 'test.map.sk/obj {:kind "pt" :name "Nikolai"})
    (valid 'test.map.sk/obj {:kind "org" :title "SPBGU"})

    (match 'test.map.sk/obj {:kind "pt" :extra "a"}
           [{:message ":name is required",
             :type "require",
             :path [:name],
             :schema ['test.map.sk/obj :schema-key 'test.map.sk/pt :require]}
            {:type "unknown-key", :message "unknown key :extra", :path [:extra]}])

    (match 'test.map.sk/obj {:kind "org" :extra "a"}
           [{:message ":title is required",
             :type "require",
             :path [:title],
             :schema ['test.map.sk/obj :schema-key 'test.map.sk/org :require]}
            {:type "unknown-key", :message "unknown key :extra", :path [:extra]}])

    )

  (testing "zen/vector"

    (valid-schema!
     {:zen/tags #{'zen/schema}
      :type 'zen/vector
      :nth {0 {:type 'zen/string}
            1 {:type 'zen/string}}})

   (invalid-schema
     {:zen/tags #{'zen/schema}
      :type 'zen/vector
      :nth {0 {:type 'zen/string}
            :ups {:type 'zen/string}}}

     [{:message "Expected type of 'integer, got 'keyword",
       :type "primitive-type",
       :path [:nth :ups],
       :schema ['zen/schema :schema-key 'zen/vector :nth :key]}])

    (zen.core/load-ns!
     tctx {'ns 'test.vec
           'nth {:zen/tags #{'zen/schema}
                 :type 'zen/vector
                 :nth {0 {:type 'zen/integer}
                       1 {:type 'zen/string}}}})


    (valid 'test.vec/nth [1 "ok"])
    (valid 'test.vec/nth [1 "ok" :anything])

    (match 'test.vec/nth ["str" "ok"]
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [0],
             :schema ['test.vec/nth :nth 0]}]))

  (testing "zen/symbol"

    (valid-schema!
     {:zen/tags #{'zen/schema}
      :type 'zen/symbol
      :tags #{'mytag}})

    (invalid-schema
     {:zen/tags #{'zen/schema}
      :type 'zen/vector
      :tags "ups"}
     [{:type "unknown-key",
       :message "unknown key :tags",
       :path [:tags]}])

    (zen.core/load-ns!
     tctx {'ns 'test.sym
           'mytag {:zen/tags #{'mytag}}
           'not-tag {}
           'sym {:zen/tags #{'zen/schema}
                 :type 'zen/symbol
                 :tags #{'mytag}}})


    (valid 'test.sym/sym 'test.sym/mytag)

    (match 'test.sym/sym 'ups
           [{:message "Expected type of 'symbol tagged '#{test.sym/mytag}, but #{}",
             :type "symbol",
             :schema ['test.sym/sym :tags]}])

    )



  )

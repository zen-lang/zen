(ns zen.schema-test
  (:require [zen.schema :as sut]
            [matcho.core :as matcho]
            [clojure.test :as t]
            [clojure.string :as str]
            [zen.core]))


(sut/register-compile-key-interpreter!
  [:keys ::ts]
  (fn [_ ztx ks]
    (fn [vtx data opts]
      (if-let [s (or (when-let [nm (:zen/name data)]
                       (str "type " (name nm) " = {"))
                     (when-let [tp (:type data)]
                       (str (name (last (:path vtx))) ": "
                            (get {'zen/string "string"}
                                 tp)
                            ";")))]
        (update vtx ::ts conj s)
        vtx))))

(sut/register-compile-key-interpreter!
  [:every ::ts]
  (fn [_ ztx every]
    (fn [vtx data opts]
      (update vtx ::ts conj "Array < "))))

(sut/register-compile-key-interpreter!
  [:type ::ts]
  (fn [_ ztx ks]
    (fn [vtx data opts]
      (-> vtx
          #_(update ::ts conj [:type (:schema vtx) (:path vtx) data])))))

(sut/register-schema-pre-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (-> vtx
          #_(update ::ts conj [:pre (:schema vtx) (:path vtx) data])))))

(sut/register-schema-post-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (if-let [nm (:zen/name data)]
        (update vtx ::ts conj "}")
        vtx))))


(t/deftest ^:kaocha/pending custom-interpreter-test
  (t/testing "typescript type generation"
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
      (str "type User = {"
           "id: string;"
           "email: string;"
           "name: Array < {"
           "given: Array < string >;"
           "family: string;"
           "}>}"))

    (def r
      (sut/apply-schema ztx
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-sturcts/User)
                        {:interpreters [::ts]}))

    (t/is (= ts-typedef-assert (str/join "" (distinct (::ts r)))))))


(defmethod sut/compile-key :my/defaults [_ _ _] {:priority -1})


(sut/register-compile-key-interpreter!
  [:my/defaults ::default]
  (fn [_ ztx defaults]
    (fn [vtx data opts]
      (update-in vtx
                 (cons ::with-defaults (:path vtx))
                 #(merge defaults %)))))


(t/deftest default-value-test
  (t/testing "set default value"
    (def ztx (zen.core/new-context {}))
    (zen.core/get-tag ztx 'zen/type)

    (def my-ns
      '{:ns my

        defaults
        {:zen/tags #{zen/schema zen/is-key}
         :zen/desc "only primitive default values are supported currently"
         :for #{zen/map}
         :priority 100
         :type zen/map
         :key {:type zen/keyword}
         :values {:type zen/case
                  :case [{:when {:type zen/boolean}}
                         {:when {:type zen/date}}
                         {:when {:type zen/datetime}}
                         {:when {:type zen/integer}}
                         {:when {:type zen/keyword}}
                         {:when {:type zen/number}}
                         {:when {:type zen/qsymbol}}
                         {:when {:type zen/regex}}
                         {:when {:type zen/string}}
                         {:when {:type zen/symbol}}]}}

        HumanName
        {:zen/tags #{zen/schema}
         :type zen/map
         :require #{:family :given}
         :keys {:given {:type zen/vector
                        :minItems 1
                        :every {:type zen/string}}
                :family {:type zen/string}}}

        DefaultHumanName
        {:zen/tags #{zen/schema}
         :type zen/map
         :my/defaults {:family "None"}}

        User
        {:zen/tags #{zen/schema}
         :type zen/map
         :my/defaults {:active true}
         :keys {:id {:type zen/string}
                :name {:type zen/vector
                       :every {:confirms #{HumanName DefaultHumanName}}}
                :active {:type zen/boolean}
                :email {:type zen/string}}}})

    (zen.core/load-ns ztx my-ns)

    #_(matcho/match (zen.core/errors ztx) #_"NOTE: FIXME: keys that use get-cached during compile time won't be recompiled when these schemas used in get-cached updated. E.g. adding new is-key for zen/schema won't cause zen/schema recompile and the key won't be recognized by zen/schema validation"
                    empty?)

    (def data
      {:id "foo"
       :email "bar@baz"
       :name [{:given ["foo"]}]})

    (def r
      (sut/apply-schema ztx
                        {::with-defaults data}
                        (zen.core/get-symbol ztx 'my/User)
                        data
                        {:interpreters [::default]}))

    (matcho/match (::with-defaults r)
                  {:id "foo"
                   :email "bar@baz"
                   :active true
                   :name   [{:family "None"
                             :given  ["foo"]}]})))

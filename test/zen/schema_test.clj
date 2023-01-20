(ns zen.schema-test
  (:require [zen.schema :as sut]
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


(t/deftest custom-interpreter-test
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
                #_#_:name {:type zen/vector
                           :every {:type zen/map
                                   :keys {:given {:type zen/vector
                                                  :every {:type zen/string}}
                                          :family {:type zen/string}}}}}}})

    (zen.core/load-ns ztx my-structs-ns)

    (def ts-typedef-assert
      (str "type User = {"
           "id: string;"
           "email: string;"
           "}"))

    (def r
      (sut/apply-schema ztx
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-sturcts/User)
                        {:interpreters [#_:zen.v2-validation/validate ::ts]}))

    (t/is (= ts-typedef-assert (str/join "" (distinct (::ts r)))))))

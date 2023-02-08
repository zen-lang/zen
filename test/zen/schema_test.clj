(ns zen.schema-test
  (:require [zen.schema :as sut]
            [clojure.test :as t]
            [clojure.string :as str]
            [zen.core]))


(sut/register-compile-key-interpreter!
  [:keys ::ts]
  (fn [_ ztx ks]
    (fn [vtx data opts] 
      (println "CMP" (:path vtx) (:type vtx) data)
      (if-let [s (or (when-let [nm (:zen/name data)]
                       (str "type " (name nm) " = "))
                     (when-let [tp (:type data)]
                       (str (name (last (:path vtx))) ": "
                            (get {'zen/string "string "}
                                 tp))))]
                            
        (update vtx ::ts conj s)
        vtx))))

(zen.schema/register-schema-pre-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
     (println "PRE" (:path vtx) (:type vtx) data)
     (cond (= (last (:path vtx)) :keys) (update vtx ::ts conj "{ ")
           (and (= (:type data) 'zen/vector) (= (:type vtx) 'zen/map)) (update vtx ::ts conj "Array<")))))

(zen.schema/register-schema-post-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
     (println "PST" (:path vtx) (:type vtx) data)
     (cond (= (last (:path vtx)) :keys) (update vtx ::ts conj " } ")
           (and (= (:type data) 'zen/vector) (= (:type vtx) 'zen/map)) (update vtx ::ts conj ">")))))



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
                :nested_map {:type zen/map 
                             :keys {:a {:type zen/string} :b {:type zen/map :keys {}} }}
                :nested_array {:type zen/vector :every {:type zen/map :keys {}}}
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

    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))


;; (sut/register-compile-key-interpreter!
;;   [:my/defaults ::default]
;;   (fn [_ ztx defaults]
;;     (fn [vtx data opts]
;;       (update-in vtx
;;                  (cons ::with-defaults (:path vtx))
;;                  #(merge defaults %)))))


;; (t/deftest default-value-test
;;   (t/testing "set default value"
;;     (def ztx (zen.core/new-context {}))

;;     (def my-ns
;;       '{:ns my

;;         defaults
;;         {:zen/tags #{zen/property zen/schema}
;;          :type zen/boolean}

;;         User
;;         {:zen/tags #{zen/schema}
;;          :type zen/map
;;          :my/defaults {:active true}
;;          :keys {:id {:type zen/string}
;;                 :active {:type zen/boolean}
;;                 :email {:type zen/string}}}})

;;     (zen.core/load-ns ztx my-ns)

;;     (def data
;;       {:id "foo"
;;        :email "bar@baz"})

;;     (def r
;;       (sut/apply-schema ztx
;;                         {::with-defaults data}
;;                         (zen.core/get-symbol ztx 'my/User)
;;                         data
;;                         {:interpreters [::default]}))

;;     (t/is (:active (::with-defaults r)))))

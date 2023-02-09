(ns zen.schema-test
  (:require [zen.schema :as sut]
            [clojure.test :as t]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [zen.core]))

(sut/register-schema-pre-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (let [root? (nil? (:visited vtx))
            property? (and (not (symbol? data))
                           (or (:type vtx)
                               (and (nil? (:type vtx))
                                    (= :values (last (:schema vtx)))))
                           (not= :every (last (:path vtx)))
                           (not= 'zen/symbol (:type vtx)))
            property (last (:path vtx))
            is-every? (:every data)]
        (cond
          root? (update vtx ::ts conj (format "type %s =" (name (:zen/name data))))
          (= [:keys] (:path vtx)) (update vtx ::ts conj "{")
          is-every?
          (cond
            (= :values (last (:schema vtx)))
            (update vtx ::ts conj (format "%s: Array<" (name property)))
            (and (nil? (:type vtx)) (= 'zen/map (get-in data [:every :type])))
            (update vtx ::ts conj "{")
            (and (nil? (:type vtx)) (= 'zen/string (get-in data [:every :type])))
            (let [enum (get-in data [:every :enum])]
              (cond
                (and enum (= 'zen/vector (last (:schema vtx)))) vtx
                enum (update vtx ::ts conj (str (str/join " | " (map #(format "'%s'" (:value %)) enum)) ";"))
                :else (update vtx ::ts conj "string")))
            (= 'zen/map (:type vtx)) vtx
            :else (do
                    (println :WTF-every)
                    (pp/pprint (select-keys vtx [:path :type :schema]))
                    (pp/pprint data)
                    (println)
                    vtx))
          property? (cond
                      (= 'zen/string (:type data))
                      (let [enum (:enum data)]
                        (cond
                          (and enum (= :values (last (:schema vtx)))) vtx
                          enum (update vtx ::ts conj
                                       (format "%s: %s;" (name property) (str/join " | " (map #(format "'%s'" (:value %)) enum))))
                          :else (update vtx ::ts conj (format "%s: %s;" (name property) "string")))))

          :else (do
                  ;(println :WTF-general)
                  ;(pp/pprint vtx)
                  ;(pp/pprint data)
                  ;(println)
                  vtx))))))

(sut/register-schema-post-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (cond
        (= ['zen/schema :schema-key 'zen/map] (:schema vtx)) (update vtx ::ts conj "};")
        (and (nil? (:type vtx))
             (= 'zen/vector (last (:schema vtx)))
             (= 'zen/map (get-in data [:every :type])))
        (update vtx ::ts conj "}>;")
        (and (nil? (:type vtx))
             (= 'zen/vector (last (:schema vtx))))
        (update vtx ::ts conj ">;")
        :else (do
                (pp/pprint (select-keys vtx [:path :type :schema]))
                (pp/pprint data)
                (println)
                vtx)))))



(t/deftest ^:kaocha/pending custom-interpreter-test
  (t/testing "typescript type generation"
    (def ztx (zen.core/new-context {}))

    (def my-structs-ns
      '{:ns my-structs

        User
        {:zen/tags #{zen/schema}
         :type     zen/map
         :keys     {:id    {:type zen/string}
                    :email {:type zen/string}
                    :role  {:type zen/string
                            :enum [{:value "admin"} {:value "qa"}]}
                    :name  {:type  zen/vector
                            :every {:type zen/map
                                    :keys {:given  {:type  zen/vector
                                                    :every {:type zen/string}}
                                           :family {:type zen/string}}}}}}})

    (zen.core/load-ns ztx my-structs-ns)

    (def ts-typedef-assert
      (str "type User ="
           "{"
           "id: string;"
           "email: string;"
           "role: 'admin' | 'qa';"
           "name: Array<"
           "{"
           "given: Array<"
           "string"
           ">;"
           "family: string;"
           "}>;"
           "};"))

    (def r
      (sut/apply-schema ztx
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-structs/User)
                        {:interpreters [::ts]}))
    (println (clojure.string/join "\n" (::ts r)))

    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))

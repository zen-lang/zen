(ns zen.changes-test
  (:require [zen.changes :as sut]
            [clojure.test :as t]
            [matcho.core :as matcho]))



(t/deftest sch-seq-test
  (t/testing "sch tree seq correct paths unit-test"
    (matcho/match
      (sut/sch-seq
        '{:zen/tags #{zen1/schema}
          :type zen2/map
          :confirms #{foo}
          :zen/desc "2"
          :keys {:a {:type zen3/string
                     :zen/desc "3"}
                 :b {:type zen4/vector
                     :zen/desc "4"
                     :every {:type zen5/map
                             :zen/desc "5"
                             :keys {:c {:zen/desc "6"
                                        :type zen6/any}}}}}})
      [{:path [nil], :value [:zen/tags #{'zen1/schema}]}
       {:path [nil], :value [:type]}
       {:path [nil], :value [:confirms]}
       {:path [nil], :value [:zen/desc]}
       {:path [:keys :a], :value [:type]}
       {:path [:keys :a], :value [:zen/desc]}
       {:path [:keys :b], :value [:type]}
       {:path [:keys :b], :value [:zen/desc]}
       {:path [:keys :b :every], :value [:type]}
       {:path [:keys :b :every], :value [:zen/desc]}
       {:path [:keys :b :every :keys :c], :value [:zen/desc]}
       {:path [:keys :b :every :keys :c], :value [:type]}
       nil])))


(t/deftest ^:kaocha/pending changes-test
  (t/testing "ns remove"
      (def old-ztx
        {:ns '{a {ns a}}})

      (def new-ztx
        {:ns '{a {ns a}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :ok
                     :errors empty?}))

  (t/testing "ns remove"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}}
               b {ns b}}})

      (def new-ztx
        {:ns '{a {ns a}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error,
                     :errors
                     [{:type :namespace/lost,
                       :namespaces '#{b}}
                      nil]}))

  (t/testing "symbol remove"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/string}}}})

      (def new-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym2}}}
               b {ns b
                  sym2 {:zen/tags #{zen/schema}
                        :type zen/string}}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error,
                     :errors
                     [{:type :symbol/lost,
                       :ns-syms '{b #{sym}}}]}))

  #_(t/testing "schema breaking"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/number}}}})

      (def new-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/integer}}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error
                     :errors [{}
                              nil]})))

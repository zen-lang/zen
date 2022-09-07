(ns zen.changes-test
  (:require [zen.changes :as sut]
            [zen.core]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(t/deftest ^:kaocha/pending sch-seq-test
  (t/testing "any dsl traversing"
    (def ztx (zen.core/new-context))

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{zen/schema}
          :type zen/map
          :confirms #{foo}
          :zen/desc "2"
          :keys {:a {:type zen/string
                     :zen/desc "3"}
                 :b {:type zen4/vector
                     :zen/desc "4"
                     :every {:type zen/map
                             :zen/desc "5"
                             :keys {:c {:zen/desc "6"
                                        :type zen/any}}}}}})
      [{:path [nil], :value [:zen/tags #{'zen/schema}]} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       {:path [nil], :value [:type]}
       {:path [nil], :value [:confirms]}
       {:path [nil], :value [:zen/desc]}
       {:path [:keys :a nil], :value [:type]}
       {:path [:keys :a nil], :value [:zen/desc]}
       {:path [:keys :b nil], :value [:type]}
       {:path [:keys :b nil], :value [:zen/desc]}
       {:path [:keys :b :every nil], :value [:type]}
       {:path [:keys :b :every nil], :value [:zen/desc]}
       {:path [:keys :b :every :keys :c nil], :value [:zen/desc]}
       {:path [:keys :b :every :keys :c nil], :value [:type]}
       nil])

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{api}
          :GET search
          "$export" {:POST export}
          [:id] {:GET read}})
      [{:path [:GET nil], :value ['search nil]}
       {:path [[:id] :GET nil], :value ['read nil]}
       nil])

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{rpc}
          :zen/desc "Get status of the bucket loading progress/state"
          :params {:type zen/map
                   :require #{:foo}
                   :keys {:foo {:type zen/string}}}
          :result {:type zen/map
                   :require #{:bar}
                   :keys {:bar {:type zen/string}}}})
      [{:path [:params nil], :value [:type 'zen/map]}
       {:path [:params nil], :value [:require #{:foo}]}
       {:path [:params :keys :foo], :value [:type 'zen/string]}
       {:path [:result nil], :value [:type 'zen/map]}
       {:path [:result nil], :value [:require #{:bar}]}
       {:path [:result :keys :bar], :value [:type 'zen/string]}
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

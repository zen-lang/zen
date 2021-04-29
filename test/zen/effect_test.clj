(ns zen.effect-test
  (:require [matcho.core :as matcho]
            [clojure.test :refer [deftest is testing]]
            [zen.test-utils :refer [vmatch match valid valid-schema! invalid-schema]]
            [zen.core]))


(deftest effect-tests
  (def tctx (zen.core/new-context {:unsafe true}))


  (testing "keyname-schema"
    (zen.core/load-ns!
     tctx {'ns 'test.kns

           'mytag {:zen/tags #{'zen/tag}}

           'sch-1 {:zen/tags #{'mytag 'zen/schema}
                   :type 'zen/integer}

           'sch-2 {:zen/tags #{'mytag 'zen/schema}
                   :type 'zen/string}

           'kns {:zen/tags #{'zen/schema}
                 :type 'zen/map
                 :keyname-schemas {:tags #{'mytag}}}})

    (valid tctx 'test.kns/kns {:test.kns/sch-1 1 :test.kns/sch-2 "ok"})

    (match tctx 'test.kns/kns {:test.kns/sch-1 "a" :test.kns/sch-2 1}
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [:test.kns/sch-1],
             :schema ['test.kns/kns :keyname-schemas :test.kns/sch-1]}
            {:message "Expected type of 'string, got 'long",
             :type "string.type",
             :path [:test.kns/sch-2],
             :schema ['test.kns/kns :keyname-schemas :test.kns/sch-2]}])


    (match tctx 'test.kns/kns {:test.kns/sch-1 1 :test.kns/sch-2 "ok" :extra "ups"}
           [{:type "unknown-key", :message "unknown key :extra", :path [:extra]}]))

  (testing "effects"
    (zen.core/load-ns!
     tctx {'ns 'test.fx
           'or {:zen/tags #{'zen/schema-fx 'zen/schema}
                :type     'zen/vector
                :every    {:type 'zen/keyword}}

           'just {}

           'subj {:zen/tags #{'zen/schema}
                  :type     'zen/map
                  :keys     {:name  {:type 'zen/string}
                             :email {:type 'zen/string}}
                  'or       [:name :email]}})

    (invalid-schema tctx
     {:zen/tags     #{'zen/schema}
      :type         'zen/map
      'test.fx/just [:name :email]}
     [{:type    "unknown-key",
       :message "unknown key test.fx/just",
       :path    ['test.fx/just]}])

    (valid-schema! tctx
     {:zen/tags   #{'zen/schema}
      :type       'zen/map
      'test.fx/or [:name :email]})

    (invalid-schema tctx
     {:zen/tags   #{'zen/schema}
      :type       'zen/map
      'test.fx/or 1}
     [{:message "Expected type of 'vector, got long",
       :type    "type",
       :path    ['test.fx/or],
       :schema  ['zen/schema :keyname-schemas 'test.fx/or]}])

    (vmatch tctx
            '#{test.fx/subj}
            {:name "Ilya"}
                  {:errors  empty?
                   :effects [{:fx     'test.fx/or
                              :path   ['test.fx/or]
                              :data   {:name "Ilya"}
                              :params [:name :email]}
                             nil?]}))
  )

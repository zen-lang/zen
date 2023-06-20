(ns zen.effect-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core]
   [zen.effect]
   [zen.test-utils :as utils]))

(t/deftest keyname-schemas
  (def tctx (zen.core/new-context {:unsafe true}))

  (zen.core/load-ns!
   tctx {'ns 'test.kns

         'mytag {:zen/tags #{'zen/tag}}

         'sch-1 {:zen/tags #{'mytag 'zen/schema}
                 :type     'zen/integer}

         'sch-2 {:zen/tags #{'mytag 'zen/schema}
                 :type     'zen/string}

         'kns {:zen/tags        #{'zen/schema}
               :type            'zen/map
               :keyname-schemas {:tags #{'mytag}}}})

  (utils/valid tctx 'test.kns/kns {:test.kns/sch-1 1 :test.kns/sch-2 "ok"})

  (utils/match tctx 'test.kns/kns {:test.kns/sch-1 "a" :test.kns/sch-2 1}
               [{:message "Expected type of 'integer, got 'string"
                 :type "integer.type"
                 :path    [:test.kns/sch-1],
                 :schema  ['test.kns/kns :keyname-schemas :test.kns/sch-1 :type]}
                {:message "Expected type of 'string, got 'long",
                 :type    "string.type",
                 :path    [:test.kns/sch-2],
                 :schema  ['test.kns/kns :keyname-schemas :test.kns/sch-2 :type]}])

  (utils/match tctx 'test.kns/kns {:test.kns/sch-1 1 :test.kns/sch-2 "ok" :extra "ups"}
               [{:type "unknown-key", :message "unknown key :extra", :path [:extra]}]))

(t/deftest validate-effect
  (def tctx (zen.core/new-context {:unsafe true}))

  (zen.core/load-ns!
   tctx {'ns  'test.fx
         'only-one {:zen/tags #{'zen/schema-fx 'zen/schema}
                    :type     'zen/vector
                    :every    {:type 'zen/keyword}}

         'just {}})

  (utils/invalid-schema tctx
                        {:zen/tags     #{'zen/schema}
                         :type         'zen/map
                         'test.fx/just [:name :email]}
                        [{:type    "unknown-key",
                          :message "unknown key test.fx/just",
                          :path    ['test.fx/just]}])

  (utils/valid-schema! tctx
                       {:zen/tags    #{'zen/schema}
                        :type        'zen/map
                        'test.fx/only-one [:name :email]})

  (utils/invalid-schema tctx
                        {:zen/tags    #{'zen/schema}
                         :type        'zen/map
                         'test.fx/only-one 1}
                        [{:message "Expected type of 'vector, got 'long"
                          :path ['test.fx/only-one]
                          :type "vector.type"
                          :schema ['zen/schema :keyname-schemas 'test.fx/only-one :type]}]))

(t/deftest emit&apply-effect
  (def tctx (zen.core/new-context {:unsafe true}))

  (zen.core/load-ns!
   tctx {'ns   'test.fx
         'only-one  {:zen/tags #{'zen/schema-fx 'zen/schema}
                     :type     'zen/vector
                     :every    {:type 'zen/keyword}}
         'subj {:zen/tags #{'zen/schema}
                :type     'zen/map
                :keys     {:name  {:type 'zen/string}
                           :email {:type 'zen/string}}
                'only-one      [:name :email]}})

  (def data {:name "Ilya", :email "ir4y.ix@gmail.com"})

  (def validation-result (zen.core/validate tctx '#{test.fx/subj} data))

  (matcho/match validation-result
    {:errors  empty?
     :effects [{:name   'test.fx/only-one
                :path   ['test.fx/only-one]
                :data   {:name "Ilya", :email "ir4y.ix@gmail.com"}
                :params [:name :email]}
               nil?]})

  (defmethod zen.effect/fx-evaluator 'test.fx/only-one [_ctx {:keys [path params]} data]
    (when (< 1 (count (select-keys data params)))
      {:data data
       :errors [{:path    path
                 :type    "effect"
                 :message "Should be either :name or :email not both at once"}]}))

  (matcho/match (zen.effect/apply-fx tctx validation-result data)
    {:errors  [{:path    ['test.fx/only-one]
                :type    "effect"
                :message "Should be either :name or :email not both at once"}
               nil?]
     :effects empty?
     :data    {:name "Ilya", :email "ir4y.ix@gmail.com"}})

  (matcho/match (zen.effect/apply-fx tctx (zen.core/validate tctx '#{test.fx/subj} {:name "Ilya"}) {:name "Ilya"})
    {:errors empty?
     :effects empty?}))

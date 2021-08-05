(ns zen.validation-test
  (:require [matcho.core :as matcho]
            [clojure.test :refer [deftest is testing]]
            [zen.test-utils :refer [vmatch match valid valid-schema! invalid-schema]]
            [zen.core]))

(def tctx (zen.core/new-context {:unsafe true}))

(zen.core/load-ns!
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

       'open-map {:zen/tags #{'zen/schema}
                  :type 'zen/map
                  :validation-type :open
                  :keys {:fixed {:type 'zen/string}}}

       'closed-map {:zen/tags #{'zen/schema}
                  :type 'zen/map
                  :validation-type :closed
                  :keys {:fixed {:type 'zen/string}}}

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
                     :old-name {:fail "Not supported"}
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

       'some-superset {:type 'zen/set
                       :superset-of #{"foo" "bar" "baz"}}

       'some-subset {:type 'zen/set
                     :subset-of '#{"foo" "bar" "baz"}}

       'some-number {:type 'zen/number}})

;; (get-in @tctx [:syms 'myapp/User])
(deftest test-validation
  (is (empty? (:errors @tctx)))

  (vmatch tctx #{'myapp/str} 1
          {:errors [{:message #"Expected type of 'string"}]})

  (vmatch tctx #{'myapp/str} "string"
          {:errors empty?})

  (vmatch tctx #{'myapp/User} 1
          {:errors [{:message #"Expected type of 'map"}]})

  (vmatch tctx #{'myapp/User} {:name 1}
          {:errors [{:message #"Expected type of 'string"
                     :path [:name]
                     :schema ['myapp/User :name]}
                    nil?]})

  (vmatch tctx #{'myapp/User} {:name "niquola"}
          {:errors empty?})

  (vmatch tctx #{'myapp/User} {:id "niquola"}
          {:errors [{:message #":name is required"
                     :path [:name]
                     :schema ['myapp/User :require]}]})

  (vmatch tctx #{'myapp/User} {:name "niquola" :role "niquola"}
          {:errors [{:path [:role]
                     :type "unknown-key"
                     :message "unknown key :role"}]})

  (vmatch tctx #{'myapp/User} {:name "niquola" :address {:extra "niquola"}}
          {:errors [{:path [:address :city]
                     :schema ['myapp/User :address :confirms 'myapp/Address :require]
                     :type "require"
                     :message ":city is required"}
                    {:path [:address :extra]
                     :type "unknown-key"
                     :message "unknown key :extra"}]})

  (vmatch tctx #{'myapp/User} {:name "niquola" :contact {:phone "888"}}
          {:errors
           [{:type "unknown-key",
             :message "unknown key :contact",
             :path [:contact]}]})

  (vmatch tctx #{'myapp/SuperUser} {:name "niquola" :role "niquola"}
          {:errors empty?})

  (vmatch tctx #{'myapp/SuperUser} {:name "niquola" :contact {:phone "888"}}
          {:errors
           [{:message ":ex is required",
             :type "require",
             :path [:contact :ex],
             :schema ['myapp/SuperUser :contact :require]}
            nil?]})

  (vmatch tctx #{'myapp/User} {:name "vganshin" :old-name "rgv"}
          {:errors [{:message #"Not supported"}]})


  (vmatch tctx #{'myapp/User} {:name "niquola" :address {:line "ups" :city "city"}}
          {:errors
           [{:message #"Expected type of 'vector",
             :type "type",
             :path [:address :line],
             :schema ['myapp/User :address :confirms 'myapp/Address :line]}
            nil?]})

  (match tctx 'myapp/User
         {:name "niquola" :address {:line ["a" "b" 1] :city "city"}}
         [{:message "Expected type of 'string, got 'long",
           :type "string.type",
           :path [:address :line 2],
           :schema ['myapp/User :address :confirms 'myapp/Address :line :every]}
          nil?])

  (zen.core/load-ns!
   tctx {'ns 'elcheck

         'Person
         {:zen/tags #{'zen/schema}
          :type 'zen/map
          :values {:type 'zen/any}
          :keys {:firstName {:type 'zen/string}
                 :lastName {:type 'zen/string}
                 :middleName {:type 'zen/string}
                 :birthDate {:type 'zen/datetime}
                 :genderCode {:type 'zen/string
                              :enum [{:value "M" :zen/desc "Male"}
                                     {:value "F" :zen/desc "Female"}]}}}

         'Elcheck
         {:zen/tags #{'zen/schema}
          :type 'zen/map
          :values {:type 'zen/any}
          :keys {:patient {:confirms #{'Person}}
                 :subscriber {:confirms #{'Person}
                              :type 'zen/map
                              :keys {:memberId {:type 'zen/string}}}}}})

  (vmatch tctx #{'elcheck/Elcheck}
          {:patient {:genderCode "F",
                    :updateYourRecords true,
                    :subscriberRelationship "Spouse",
                    :birthDate "1970-01-01T00:00:00.000+0000",
                    :firstName "Nat",
                    :id "777",
                    :lastName "Color",
                    :gender "Female",
                    :subscriberRelationshipCode "01"},
          :subscriber
          {:id "22" :lastName "O'DONNELL", :memberId "VMPV22071045", :firstName "RYAN"}}
         {:errors [nil?]})

  (vmatch tctx #{'myapp/User} {:name "niquola" :identifiers [1 {:ups "ups"}]}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:identifiers 0],
             :schema ['myapp/User :identifiers :every :confirms 'myapp/Identifier]}
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

  (vmatch tctx #{'myapp/User} {:name "niquola" :identifiers [{:system "s1" :value "v1" :extra "value"}
                                                        {:system "s1" :value "v2"}]}
          {:errors empty?})

  (vmatch tctx #{'myapp/User}
          {:name "niquola"
           :myapp/unexisting "ups"
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          {:errors
           [{:type "unknown-key",
             :message "unknown key :myapp/unexisting",
             :path [:myapp/unexisting]}]})

  (vmatch tctx #{'myapp/User}
          {:name "niquola"
           :myapp/mykey "hi"
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          {:errors empty?})

  (match tctx 'myapp/User
         {:name "niquola"
          :myapp/mykey 1
          :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
         [{:message "Expected type of 'string, got 'long",
           :type "string.type",
           :path [:myapp/mykey],
           :schema ['myapp/User :property :myapp/mykey]}])

  (vmatch tctx #{'myapp/User} {:name "niquola" :identifiers [{:system "s1" :value "v1" :extra "value"}]}
          {:errors [{:message "Expected >= 2, got 1" :path [:identifiers] :schema ['myapp/User :identifiers :minItems]}]})

  (vmatch tctx #{'zen/schema} {:type 'zen/string}
          {:errors empty?})

  (vmatch tctx #{'zen/schema} {:type 'zen/map :keys 1}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:keys],
             :schema ['zen/schema :schema-key 'zen/map :keys]}]})

  (vmatch tctx #{'myapp/Settings} {:headers {:a "str"}}
          {:errors empty?})

  (vmatch tctx #{'myapp/Settings} {:headers {:a 1}}
          {:errors [{:path [:headers :a]
                     :schema ['myapp/Settings :headers :values]
                     :message #"Expected type of 'string"}]})

  (match tctx 'myapp/Settings
         {:headers {:content-type "up" :a "up"}}
         [{:message "Expected length >= 3, got 2",
           :type "string.minLength",
           :path [:headers :content-type],
           :schema ['myapp/Settings :headers :content-type :minLength]}])

  (vmatch tctx #{'zen/schema} {:type 'zen/map :keys {:prop 1}}
          {:errors
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :path [:keys :prop],
             :schema ['zen/schema :schema-key 'zen/map :keys :values]}]})

  (vmatch tctx #{'zen/schema} {:type 'zen/map
                          :keys {:ups {:type 'zen/string
                                       :minLength "ups"}}}
          {:errors
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [:keys :ups :minLength],
             :schema
             ['zen/schema :schema-key 'zen/map :keys :values :confirms 'zen/schema :schema-key 'zen/string :minLength]}]})


  (vmatch tctx #{'zen/schema}
          {:type 'zen/map
           :keys {:minLength {:type 'zen/integer, :min 0}
                  :maxLength {:type 'zen/integer, :min 0}
                  :regex {:type 'zen/regex}}}
          {:errors empty?})

  (vmatch tctx #{'zen/schema}
          {:type 'zen/map
           :keys {:minLength {:type 'zen/integer, :ups 0}
                  :maxLength {:type 'zen/integer, :min ""}
                  :regex {:type 'zen/regex}}}

          {:errors
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [:keys :maxLength :min],
             :schema ['zen/schema :schema-key 'zen/map :keys :values :confirms 'zen/schema :schema-key 'zen/integer :min]}
            {:type "unknown-key",
             :message "unknown key :ups",
             :path [:keys :minLength :ups]}]})

  (match tctx 'myapp/open-map {:foo "bar"} empty?)
  (match tctx 'myapp/closed-map {:foo "bar"} [{:path [:foo]}])

  (match tctx 'myapp/email
         "ups"
         [{:message "Expected match /^.*@.*$/, got \"ups\"",
           :type "string.regex",
           :path [],
           :schema ['myapp/email :regex]}])

  (vmatch tctx #{'zen/schema}
          {:type 'zen/case
           :case [{:when {:type 'zen/integer}
                   :then {:type 'zen/integer}}
                  {:when {:type 'zen/map}
                   :then {:type 'zen/map :keys {:name {:type 'zen/string}}}}]}
          {:errors empty?})

  (vmatch tctx #{'myapp/path}
          ["string" {:name "name"} "rest"]
          {:errors empty?})

  (vmatch tctx #{'myapp/path}
          ["string" {:name "name"} 1 {:name "ups"}]
          {:errors
           [{:message
             "Expected one of ({:type zen/string} {:type zen/map}), but none is conformant",
             :type "case",
             :path [2],
             :schema ['myapp/path :every :case]}]})

  (vmatch tctx #{'myapp/path}
          ["string" {:name "name"} 1 {:name "ups"}]
          {:errors
           [{:message
             "Expected one of ({:type zen/string} {:type zen/map}), but none is conformant",
             :type "case",
             :path [2],
             :schema ['myapp/path :every :case]}]})


  (vmatch tctx #{'myapp/maps-case}
          {:person true :name "Ok"}
          {:errors empty?})

  (vmatch tctx #{'myapp/maps-case}
          {:person true}
          {:errors
           [{:message ":name is required",
             :type "require",
             :path [:name],
             :schema ['myapp/maps-case :case 0 :then :require]}]})

  (vmatch tctx #{'myapp/maps-case}
          {:org true}
          {:errors
           [{:message ":title is required",
             :type "require",
             :path [:title],
             :schema ['myapp/maps-case :case 1 :then :require]}]})

  (vmatch tctx #{'myapp/maps-case}
          {:something true}
          {:errors
           [{:message
             "Expected one of ({:type zen/map, :require #{:person}, :keys {:person {:type zen/boolean}}} {:type zen/map, :require #{:org}, :keys {:org {:type zen/boolean}}}), but none is conformant",
             :type "case",
             :path [],
             :schema ['myapp/maps-case :case]}]})

  (vmatch tctx #{'myapp/const} "ups"
          {:errors
           [{:message "Expected 'fixed', got 'ups'",
             :type "schema",
             :path [],
             :schema ['myapp/const]}]})

  (vmatch tctx #{'myapp/const} "fixed"
          {:errors empty?})

  (vmatch tctx #{'myapp/const} 1
          {:errors
           [{:message "Expected 'fixed', got '1'",
             :schema ['myapp/const]}
            {:message "Expected type of 'string, got 'long",
             :schema ['myapp/const]}]})

  (vmatch tctx #{'myapp/const-map} {:fixed "fixed"}
          {:errors empty?})

  (vmatch tctx #{'myapp/const-map} {:fixed "ups"}
          {:errors
           [{:message "Expected '{:fixed \"fixed\"}', got '{:fixed \"ups\"}'",
             :type "schema",
             :path [],
             :schema ['myapp/const-map]}]})

  ;; (vmatch tctx #{'zen/schema}
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

    (valid-schema! tctx str-sch)

    (invalid-schema tctx
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


    (zen.core/load-ns! tctx {'ns 'test.str
                             'str str-sch
                             'mytag  {:zen/tags #{'zen/tag}}
                             'tagged {:zen/tags #{'mytag}}
                             'str-tgs {:type 'zen/string :tags #{'mytag}}})

    (zen.core/get-symbol tctx 'test.str/tagged)
    @tctx

    (valid tctx 'test.str/str "a@a")

    (vmatch tctx #{'test.str/str-tgs} "zen/map"
            {:errors [{:message #"Expected symbol 'zen/map tagged .*"}]})

    (vmatch tctx #{'test.str/str-tgs} "nonexits/one"
            {:errors [{:message #".*No symbol.*"}]})

    (valid tctx 'test.str/str-tgs "test.str/tagged")

    )

  ;; tessting map
  (testing "zen/map"

    (valid-schema! tctx
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

    (valid tctx 'test.map/just-map {:a 1})

    (match tctx 'test.map/just-map 1
           [{:message "Expected type of 'map, got 1",
             :type "type",
             :schema ['test.map/just-map]}])

    (valid tctx 'test.map/str-keys {"a" 1 "b" 2})
    (match tctx 'test.map/str-keys {:a 1 "b" 2}
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
             :require #{:title :id}
             :keys {:title {:type 'zen/string}
                    :id {:type 'zen/string}}}

       'obj {:zen/tags #{'zen/schema}
             :type 'zen/map
             :schema-key {:key :kind :ns "test.map.sk"}
             :keys {:kind {:type 'zen/string}}}

       'obj2 {:zen/tags #{'zen/schema}
              :type 'zen/map
              :schema-key {:key :kind}
              :keys {:kind {:type 'zen/string}}}})

    (valid-schema! tctx (get sk-sch 'obj))

    (zen.core/load-ns! tctx sk-sch)

    (valid tctx 'test.map.sk/obj {:kind "pt" :name "Nikolai"})
    (valid tctx 'test.map.sk/obj {:kind "org" :title "SPBGU" :id "org"})
    (valid tctx 'test.map.sk/obj2 {:kind "test.map.sk/pt" :name "Nikolai"})
    (valid tctx 'test.map.sk/obj2 {:kind "test.map.sk/org" :title "SPBGU" :id "org"})

    (match tctx 'test.map.sk/obj {:kind "pt" :extra "a"}
           [{:message ":name is required",
             :type "require",
             :path [:name],
             :schema ['test.map.sk/obj :schema-key 'test.map.sk/pt :require]}
            {:type "unknown-key", :message "unknown key :extra", :path [:extra]}])

    (match tctx 'test.map.sk/obj {:kind "org" :extra "a"}
           [{:message ":title is required",
             :type "require",
             :path [:title],
             :schema ['test.map.sk/obj :schema-key 'test.map.sk/org :require]}
            {:message ":id is required",
             :type "require",
             :path [:id],
             :schema ['test.map.sk/obj :schema-key 'test.map.sk/org :require]}
            {:type "unknown-key", :message "unknown key :extra", :path [:extra]}])

    )

  (testing "zen/vector"

    (valid-schema! tctx
     {:zen/tags #{'zen/schema}
      :type 'zen/vector
      :nth {0 {:type 'zen/string}
            1 {:type 'zen/string}}})

    (invalid-schema tctx
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


    (valid tctx 'test.vec/nth [1 "ok"])
    (valid tctx 'test.vec/nth [1 "ok" :anything])

    (match tctx 'test.vec/nth ["str" "ok"]
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [0],
             :schema ['test.vec/nth :nth 0]}]))

  (testing "zen/symbol"

    (valid-schema! tctx
     {:zen/tags #{'zen/schema}
      :type 'zen/symbol
      :tags #{'zen/tag}})

    (valid-schema!
     tctx
     {:zen/tags #{'zen/schema}
      :type 'zen/string
      :tags #{'zen/tag}})

    (invalid-schema tctx
     {:zen/tags #{'zen/schema}
      :type 'zen/vector
      :tags "ups"}
     [{:type "unknown-key",
       :message "unknown key :tags",
       :path [:tags]}])

    (zen.core/load-ns!
     tctx {'ns 'test.sym
           'mytag {:zen/tags #{'mytag 'zen/tag}}
           'not-tag {}
           'sym {:zen/tags #{'zen/schema}
                 :type 'zen/symbol
                 :tags #{'mytag}}})


    (valid tctx 'test.sym/sym 'test.sym/mytag)

    (match tctx 'test.sym/sym 'ups
           [{:message "Expected symbol 'ups tagged with '#{test.sym/mytag}, but only #{}"
             :type "symbol",
             :schema ['test.sym/sym :tags]}])


    (zen.core/load-ns!
     tctx
     '{ns test.enum-and-const
       one
       {:zen/tags #{zen/schema}
        :type zen/number}

       date
       {:zen/tags #{zen/schema}
        :type zen/date}

       datetime
       {:zen/tags #{zen/schema}
        :type zen/datetime}})

    (valid tctx 'test.enum-and-const/one 1)
    (valid tctx 'test.enum-and-const/one 1.1)

    (valid tctx 'test.enum-and-const/date "1994-09-26")

    (valid tctx 'test.enum-and-const/datetime "1994-09-26T16:40:00")
    (valid tctx 'test.enum-and-const/datetime "1994-09-26")


    (zen.core/load-ns!
     tctx
     '{ns custom-primitive-type
       animal
       {:zen/tags #{zen/type zen/primitive zen/schema}
        :type zen/map}

       cow
       {:zen/tags #{zen/schema}
        :type animal}})

    (match tctx 'custom-primitive-type/cow {:cow "mooo"}
           [{:message "No validate-type multimethod for 'custom-primitive-type/animal",
             :type "primitive-type",
             :path [],
             :schema ['custom-primitive-type/cow]}])

    (zen.core/load-ns!
     tctx {'ns 'test.enum
           'val {:zen/tags #{'zen/schema}
                 :type 'zen/string
                 :enum [{:value "a1"}
                        {:value "a2"}]}
           'inh-val {:zen/tags #{'zen/schema}
                     :type 'zen/string
                     :confirms #{'val}
                     :enum [{:value "b1"}
                            {:value "b2"}]}})

    (valid tctx 'test.enum/val "a1")
    (valid tctx 'test.enum/inh-val "a1")
    (valid tctx 'test.enum/inh-val "b1")

    (match tctx 'test.enum/val "c"
           [{:type "enum", :message "Expected 'c' in #{\"a1\" \"a2\"}", :path []}])

    (match tctx 'test.enum/inh-val "c"
           [{:type "enum",
             :message "Expected 'c' in #{\"a1\" \"b2\" \"a2\" \"b1\"}",
             :path []}])


    (zen.core/load-ns!
     tctx {'ns 'test.list

           'list {:zen/tags #{'zen/schema}
                  :type 'zen/list
                  :nth {0 {:const {:value :->}}}}

           'fn1 {:zen/tags #{'zen/schema}
                 :type 'zen/list
                 :nth {1 {:type 'zen/integer}}}

           'fn2 {:zen/tags #{'zen/schema}
                 :type 'zen/list
                 :nth {1 {:type 'zen/string}}}

           'poly-list {:zen/tags #{'zen/schema}
                       :type 'zen/list
                       :schema-index {:index 0 :ns "test.list"}}})

    (valid tctx 'test.list/list '(:-> 1 2 3))

    (match tctx 'test.list/list '(1 2 3)
           [{:message "Expected ':->', got '1'",
             :type "schema",
             :path [0],
             :schema ['test.list/list :nth 0]}])

    (valid tctx 'test.list/poly-list '(test.list/fn1 1))
    (valid tctx 'test.list/poly-list '(test.list/fn2 "str"))


    (match tctx 'test.list/poly-list '(test.list/fn1 "str")
           [{:message "Expected type of 'integer, got 'string",
             :type "primitive-type",
             :path [1],
             :schema ['test.list/poly-list :schema-index 'test.list/fn1 :nth 1]}])

    (match tctx 'test.list/poly-list '(test.list/fn2 1)
           [{:message "Expected type of 'string, got 'long",
             :type "string.type",
             :path [1],
             :schema ['test.list/poly-list :schema-index 'test.list/fn2 :nth 1]}])

    (match tctx 'test.list/poly-list '(test.list/ups 1)
           [{:message "Could not find schema test.list/ups",
             :type "schema",
             :path [],
             :schema ['test.list/poly-list :schema-index]}])

    )

  (match tctx
   'zen/schema
   {:zen/tags #{'zen/schema}
    :type 'zen/schema}
   [{:message
     "Expected symbol 'zen/schema tagged with '#{zen/type}, but only #{zen/tag zen/schema}",
     :type "symbol",
     :path [:type],
     :schema ['zen/schema :type :tags]}
    {:message
     "'zen/schema should be tagged with #{zen/type}, but #{zen/tag zen/schema}",
     :type "schema",
     :path [],
     :schema ['zen/schema]}])

  (zen.core/load-ns!
   tctx {'ns 'test.confirms

         'c1 {:zen/tags #{'zen/schema}
              :type 'zen/map
              :keys {:a {:type 'zen/string}}}

         'c2 {:zen/tags #{'zen/schema}
              :type 'zen/map
              :confirms #{'c1}
              :keys {:b {:type 'zen/string}}}

         'c3 {:zen/tags #{'zen/schema}
              :type 'zen/map
              :confirms #{'c2 'c1}
              :keys {:c {:type 'zen/string}}}

         'c4 {:zen/tags #{'zen/schema}
              :type 'zen/map
              :confirms #{'c3 'c2 'c1}
              :keys {:d {:type 'zen/string}}}
         })

  (match tctx 'test.confirms/c4 {:a 1}
         [{:message "Expected type of 'string, got 'long",
           :type "string.type",
           :path [:a],
           :schema
           ['test.confirms/c4 :confirms 'test.confirms/c2 :confirms 'test.confirms/c1 :a]}
          nil?])


  (zen.core/get-symbol tctx 'zen/fn)

  ;; :args {:type 'zen/vector
  ;;        :cat [:ctx {:confirms #{'app/ctx}}]}

  (valid-schema! tctx
   {:zen/tags  #{'zen/schema}
    :type 'zen/map
    :keys {:path {:type 'zen/apply :tags #{'zen/fn}}}})

  (zen.core/load-ns!
   tctx {'ns 'test.fn

         'fn {:zen/tags #{'zen/tag}}

         'other-fn {:zen/tags #{'zen/fn}
                    :args {:type 'zen/vector :every {:type 'zen/keyword}}
                    :ret  {:type 'zen/string}}

         'get {:zen/tags #{'zen/fn 'fn}
               :args {:type 'zen/vector :every {:type 'zen/keyword}}
               :ret  {:type 'zen/string}}

         'tpl {:zen/tags  #{'zen/schema 'zen/tag}
               :type 'zen/map
               :keys {:path {:type 'zen/apply :tags #{'fn}}}}

         'example {:zen/tags #{'tpl}
                   :path (list 'get :a :b :c)}})

  (match tctx 'test.fn/tpl {:path "1"}
         [{:message "Expected fn call '(fn-name args-1 arg-2), got 'string",
           :type "apply.type",
           :path [:path],
           :schema ['test.fn/tpl :path]}])

  (match tctx 'test.fn/tpl {:path (list 'test.fn/tpl "1")}
         [{:message
           "fn definition 'test.fn/tpl should be taged with 'zen/fn, but '#{zen/tag zen/schema}", :type "apply.fn-tag",
           :path [:path],
           :schema ['test.fn/tpl :path]}])

  (match tctx 'test.fn/tpl {:path (list 'test.fn/other-fn "1")}
         [{:message
           "fn definition 'test.fn/other-fn should be taged with #{test.fn/fn}, but '#{zen/fn}",
           :type "apply.tags",
           :path [:path],
           :schema ['test.fn/tpl :path]}])

  (match tctx 'test.fn/tpl {:path (list 'test.fn/get "1")}
         [{:message "Expected type of 'keyword, got 'string",
           :type "primitive-type",
           :path [:path 0],
           :schema ['test.fn/tpl :path 'test.fn/get :args :every]}]))


(deftest set-validation
  (testing "superset-of"
    (vmatch tctx #{'myapp/some-superset} #{"foo"}
            {:errors [{:type "set"
                       :schema ['myapp/some-superset :superset-of]}
                      nil?]})

    (vmatch tctx #{'myapp/some-superset} #{"quux"}
            {:errors [{:type "set"
                       :schema ['myapp/some-superset :superset-of]}
                      nil?]})

    (vmatch tctx #{'myapp/some-superset} #{"foo" "bar" "baz"}
            {:errors [nil?]})

    (vmatch tctx #{'myapp/some-superset} #{"foo" "bar" "baz" "quux"}
            {:errors [nil?]}))

  (testing "subset-of"
    (vmatch tctx #{'myapp/some-subset} #{"foo"}
            {:errors [nil?]})

    (vmatch tctx #{'myapp/some-subset} #{"quux"}
            {:errors [{:type "set"
                       :schema ['myapp/some-subset :subset-of]}
                      nil?]})

    (vmatch tctx #{'myapp/some-subset} #{"foo" "bar" "baz"}
            {:errors [nil?]})

    (vmatch tctx #{'myapp/some-subset} #{"foo" "bar" "baz" "quux"}
            {:errors [{:type "set"
                       :schema ['myapp/some-subset :subset-of]}
                      nil?]})))

(deftest number-validation
  (vmatch tctx #{'myapp/some-number} 1
          {:errors [nil?]})

  (vmatch tctx #{'myapp/some-number} 0.1
          {:errors [nil?]})

  (vmatch tctx #{'myapp/some-number} nil
          {:errors [{:type "primitive-type"}]})

  (vmatch tctx #{'myapp/some-number} 0.1M
          {:errors [nil?]})

  (vmatch tctx #{'myapp/some-number} 1N
          {:errors [nil?]})

  (vmatch tctx #{'myapp/some-number} 1/3
          {:errors [nil?]}))

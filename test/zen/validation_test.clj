(ns zen.validation-test
  (:require [zen.core]
            [matcho.core :as matcho]
            [clojure.test :refer [deftest is]]))

(def tctx (zen.core/new-context))

(zen.core/load-ns
 tctx {'ns 'myapp
       'str {:zen/tags #{'zen/schema}
             :type 'zen/string}

       'mykey {:zen/tags #{'zen/schema 'zen/property}
              :type 'zen/string}

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
               :regex #"^.*@.*$"}
       })

;; (get-in @tctx [:syms 'myapp/User])

(defmacro vmatch [schemas subj res]
  `(let [res# (zen.core/validate tctx ~schemas ~subj)]
     (matcho/match res# ~res)
     res#))


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

  (vmatch #{'myapp/User} {:name "niquola" :address {:line ["a" "b" 1] :city "city"}}
          {:errors
           [{:message #"Expected type of 'string",
             :type "primitive-type",
             :path [:address :line 2],
             :schema ['myapp/User :address 'myapp/Address :line :every]}
            nil?]})

  (vmatch #{'myapp/User} {:name "niquola" :identifiers [1 {:ups "ups"}]}
          {:errors
           [{:message "Expected type of 'map, got 1",
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

  (vmatch #{'myapp/User}
          {:name "niquola"
           :myapp/mykey 1
           :identifiers [{:system "s1" :value "v1" :extra "value"} {:system "s1" :value "v2"}]}
          {:errors
           [{:message "Expected type of 'string, got 'long",
             :type "primitive-type",
             :path [:myapp/mykey],
             :schema ['myapp/User :myapp/mykey]}]})

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

  (vmatch #{'myapp/Settings} {:headers {:content-type "up" :a "up"}}
          {:errors
           [{:message "Expected length >= 3, got 2",
             :type "string",
             :path [:headers :content-type],
             :schema ['myapp/Settings :headers :content-type :minLength]}
            nil?]})

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

  (vmatch #{'myapp/email}
          "ups"
          {:errors
           [{:message "Expected 'ups' matches /^.*@.*$/",
             :type "string",
             :schema ['myapp/email :regex]}]})

  )


## fhir to zen

Zen schema (zs) describes shape of data and constraints as data
It can be used for validation or as a source of meta-data
It can be extended with additional meta-data and validation rules

zs is highly influenced by JSON Schema and clojure.spec

1. zs uses global names! 
  Every schema has it's name and can be referenced 
  from another schema or zen model as 'name.space/name'

2. zs is writen in EDN ("right" JSON)

* Use namespaced symbols  as names 
  and references (like class names in Java or C#)
* Use set for set collections
* Use keywords for keys in maps

> For those who came from JSON land 
> we call "Object" a "map"
> we all "property" a "key"

## Specification

Schema is a map, where each key may be interpreted as instruction.
User may extend schema with additional keys.

Here are semantic of keys, applicable for all schemas:
`:confirms {:type zen/set :every {:type symbol :tags #{schema}}}` - inclusion of another schema (like mixins or inheritance)
`:schema-key {:type zen/map :keys {}}`, means that

```edn
{ns zen
 ;; ...
 schema {:zen/tags        #{schema tag}
         :zen/desc        "zen schema"
         :type            map
         :schema-key      {:key :type :tags #{type}}
         :keyname-schemas {:tags #{schema-fx}}
         :keys            {:schema-key {:type map 
                                        :keys {:key {:type keyword}
                                        :tags #{:type set 
                                                :every {:type symbol :tags #{schema}}}}}
                           :confirms {:type     set
                                      :zen/desc "set of schemas to confirm"
                                      :every    {:type symbol :tags #{schema}}}
                           :type     {:type symbol :tags #{type}}
                           :match    {:type any
                                      :zen/desc "Match data pattern"}
                           :const    {:type     map
                                      :zen/desc "Check constant"
                                      :keys     {:value {:type any}}}
                           ;; TODO: apply top level schema to value
                           :enum     {:type     set
                                      :zen/desc "Check value is in enum"
                                      :every    {:type map :keys {:value {:type any}}}}
                           :fail     {:type string}

                           :validation {:type     set
                                        :zen/desc "Custom validation set of functions"
                                        :every    {:type symbol :tags #{validation-fn}}}}}
 ;; ...
}
```









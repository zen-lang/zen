# zen meta store & schema

## store

* namespace
* resource

```
{
 ns myapp.something ;; namespace
 import #{some.ns}

 ;; resource
 symbol {
   ;; name myapp.something/symbol
   tags #{:zen/schema ....} ;; keyword tags - zen types

   ;; resource data
   :key "value"
   :reference some.ns/symbol
   :other-key {
      :content "here"
   }
 }

}
```


## schema

Composable, open-world schema engine.

Schema node:

* type (required) - defines interpreter and link to type specific schema
* confirms - set of other schemas to evaluate


For example `zen/map` type defines:

* values:  schema - schema to apply to all values
* keys: { key: schema } - enumeration of keys and schema for each key
* require: #{:key,...} - list of requried keys in map
* schema-key: {:key :some-key } - key to resolve schema from data



```edn
{:type zen/map 
 :confirms #{other schemas}
 :keys {:prop schema}
 :require #{:prop}
 :values schema
 }

{:type zen/string
 :minLength 3
 :maxLength 1000
 :regex #"^[1-9].**"
 }

{:type zen/vector
 :minItems 1
 :maxItems 2
 :every schema
 :nth {idx schema} ;; TODO
 }
```

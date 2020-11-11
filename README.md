# zen meta store & schema

zen project consists of set of namespaces.

On file system namespaces can be organized 
the same way as java/clojure project.

Each namespace is valid edn file with data,
describing your models.
Namespace is a map with two special symbol keys  - 'ns  and 'import

* 'ns - defines name of namespace
* 'imports - is a set of required namespaces to interpret this namespace (zen namespace is imported implicitly)

Inside namespace you can refer local symbols just by local 
name or symbols from extrnal namespace by ns-name/sym-name.

All other symbolic keys defines meta-resources.

Symbol may be tagged with keywords. Keywords used to 
organize and classify symbols.

* global keys

```
{ns myapp.something ;; namespace
 import #{ some.lib }

 :tags {}
 :desc {}
 ;; resource
 web {
   ;; name myapp.something/symbol
   :zen/tags #{:some.lib/http-server }

   ;; resource data
   :key "value"
   :reference some.ns/symbol
   :other-key {
      :content "here"
   }
 }

}
```



You can load zen project into meta-storage.
zen will validate all m

## store



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

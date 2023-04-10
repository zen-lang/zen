# Zen meta store & schema

[![Clojars Project](https://img.shields.io/clojars/v/zen-lang/zen.svg)](https://clojars.org/zen-lang/zen)

[zen-docs](https://zen-lang.github.io/tags/zen/schema.html)

## Motivation

Often application can be split into two parts
**declarative models** (DSLs) and  **imperative interpreter** (engines).

Models can be expressed with data - **Data DSLs**.
Why? Because data is easily composable, regular and introspectable.

There are a lot of successful Data DSLs in clojure ecosystem.
But often they do not play together :(.

Zen is a framework to unify Data DSLs,
make them composable and take **model driven design** to the next level.


## Model Project

Zen separate models source code from interpreters code, but 
keep it in source path using same layout as clojure.

Models are grouped into namespaces and namespaces are 
stored in [edn files](https://github.com/edn-format/edn).

Namespaces can be published as **reusable** packages.

### Namespace

Each namespace contains one or multiple models described with data.

Namespace is a map (in terms of edn)
with two special symbol keys  - 'ns  and 'import

* `ns` - defines name of namespace
* `imports` - imported namespaces

Namespaces may refer other namespaces by `imports`.
Core `zen` namespace is imported implicitly.

That's how starting from one **entry point** namespace,
your project can import only used modules and models from other packages.

Rest of symbolic keys in namespace define models.

Just like in clojure namespace you may refer one model from another located in one namespace
by short name (`symbol`) and refer between namespaces by full name - (`namespace.name/symbol`)

Example namespace:

```edn

{ns myapp.module ;; namespace name
 imports #{http pg model auth} ;; imports - TODO: think about aliases
 
 db
 {:zen/tags #{pg/config}
  :connection {:host "..." :port "..."}
  :storages #{model/patient-store}}

 ;; model
 web 
 {:zen/tags #{http/server} ;; tags set
  :port 8080
  :workers 8
  :db db
  :apis #{api http/admin-api}} 
 
 api 
 {:zen/tags #{http/api}
  :zen/desc "API definition"
  :middleware [{:type http/cors :allow #{"https://myapp.io"}}
               {:type auth/authorize}]
  :routes {
    "Patient" {:post {:op create-pt}
               :get  {:op search-pt}}
    "meta" {:get {:op http/meta}}}}

 create-pt 
 {:zen/tags #{http/create}
  :operation http/create
  :schemas #{model/patient}
  :response {
    201 {:confirms #{model/patient}}
    422 {:confirms #{http/error}}}}

 search-pt 
 {:zen/tags #{http/search}
  :operation http/search
  :store #{model/patient-store}
  :params {:query {:name {:zen/desc "Search by name" 
                          :engine http/fhir-search-param
                          :type "string" :expression "Patient.name"}
                   :ilike {:engine http/sql-search-param
                           :query "resource::text ilike {{param.value}}"}}}
  :response { 
    201 {:confirms #{model/search-bundle}} 
    422 {:confirms #{model/search-errors}}}}}
```



## Tags

Instead of introducing any kind of types and type hierarchies,
zen uses **tag system** to classify models.

You may think about tag system as non-hierarchical 
multidimensional classification (just like java interfaces).
Or as a function of meta store - 
get all models labeled with specific tag.

## Store

Model project may be loaded into **store**.
You start loading from **entry point namespace**.
All imports will be resolved, validated and loaded into store.

Store functions:
* get-symbol `ns/sym`
* get-tag `tag/name`
* read-ns `my/ns`

## Envs

Some data can be moved from edn to envs using special readers:

* #env - read string
* #env-integer - read integer
* #env-symbol - read symbol
* #env-keyword - read keyword
* #env-number  - read number
* #env-boolean  - read boolean

Before reading with envs - zen context should be initialized with envs: `(zen.core/new-context {:env {:ENV_NAME "env-value"}}))`
or system env variables should be set - `export MY_VAR='value'`.


```edn
{ns test-env

 schema
 {:zen/tags #{zen/schema zen/tag}
  :type zen/map
  :keys {:string {:type zen/string}
         :int    {:type zen/integer}
         :sym    {:type zen/symbol}
         :key    {:type zen/keyword}
         :num    {:type zen/number}
         :bool   {:type zen/boolean}}}

 model
 {:zen/tags #{schema }
  :string #env ESTR
  :int    #env-integer EINT
  :sym    #env-symbol ESYM
  :key    #env-keyword EKEY
  :num    #env-number ENUM}
  :bool   #env-boolean BOOL}
```

## Schema

Zen has built-in schema engine deeply integrated with meta-store.

The key features of zen/schema is:

* open world design - i.e. each schema validates only what it knows
* support of RDF inspired property schema - i.e. schema attached to key (only namespaced keys) -  not to a key container

```edn
{ns myapp

 Contact 
 {:zen/tags #{zen/schema}
   :type zen/map
   :keys 
   {:system {:type zen/string 
             :enum [{:value "phone"} 
                    {:value "email"}]
     :value  {:type zen/string}}}

 Contactable 
 {:zen/tags #{zen/schema}
   :type zen/map
   :keys {:contacts {:type zen/vector 
                     :every {:type map :confirms #{Contact}}}}}

 User 
 {:zen/tags #{zen/schema}
   :type zen/map
   :confirms #{Contactable}
   :require #{:id :email}
   :keys {
     :id {:type zen/string}
     :email {:type zen/string :regex ".*@.*"}
     :password {:type zen/string }}}

 ;; example of property schema
 
 human-name 
 {:zen/tags #{zen/property zen/schema}
   :type zen/map
   :keys {:family {:type zen/string} 
          :given {:type zen/vector :every {:type zen/string}}}}}

 instance
 {:id "niquola"
  :myapp/human-name {:given ["Nikolai"] :family "Ryzhikov"}
  :password "secret"}
```


### Schema Specification

Schema statements are described with maps.
Map may have a :type key, which defines how this
map is interpreted. For example `:type zen/string`
will check for string, zen/map describes map validation.

Here is list of built-in types:

* primitives
  * zen/symbol
  * zen/keyword
  * zen/string
  * zen/number
  * zen/integer
  * zen/boolean
  * zen/date
  * zen/datetime
* collections
  * zen/vector
  * zen/set
  * zen/map
  * zen/list
* zen/case

You can get all schema types by query meta-store for
zen/type tag. 
User can extend schema with new types - TBD

All schema maps may have common a keys:

* `:confirms` - set of other schemas to confirm (this is not inheritance!)
* `:enum` - polymorphic enumeration of possible values (TODO: think about terminology - reference semantic?)
* `:constant` - polymorphic fixed value validation
* `:valuesets` - like enum, but using valueset zen protocol (TBD)

Depending on type schema map may have type specific 
keys. For example :minLength and :regex for zen/string
or :keys for zen/map.


### zen/case

`zen/case` is alternative to union type,
it is more advanced and may be applied to different maps

```edn

path
{:zen/tags #{'zen/schema}
 :type 'zen/vector
 :every
 {:type 'zen/case
  :case [{:when {:type 'zen/string}}
         {:when {:type 'zen/map}
          :then {:type 'zen/map 
                 :require #{:name} 
                 :keys {:name {:type 'zen/string}}}}]}}

```

### zen/symbol

* :tags - set of symbols - check symbols refers models with

### zen/map

For example `zen/map` type defines following validation keys:

* `:values`  schema - schema to apply to all values
* `:keys` { key: schema } - enumeration of keys and schema for each key
* `:require` #{:key,...} - list of required keys in map
* `:schema-key` {:key :some-key } - key to resolve schema from data on fly


### zen/vector

Apply clojure.spec regular expressions for collections!!!!

* :every  schema - apply schema to every element in collection
* :nth {integer: schema} - apply schema to nth element
* :minItems & :maxItems - min/max items in collection 
* :filter - TODO: apply filter to collection, then apply schema to 

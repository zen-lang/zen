# zen

Zen is declarative language to describe models with data.
Zen syntax is based on extensible data notation (edn) - [see refs](https://github.com/edn-format/edn)

## Namespaces

Zen models are just a data in [edn format](https://github.com/edn-format/edn)

Models are organized into namespaces.
Namespaces are very similar to Java packages and Python modules.
Namespaces are basically maps (dictionaries) that map names to model data.
Namespace should have a name under `ns` key.

``` edn
{ 
  ns package.namespace
  ;; ...
  model-name {
    ;; model data
  }
}
```

By convention path to file should match namespace name: i.e.   `my/package/model.edn` <-> `my.package.model**

## v2

zen model is just a data in edn format

edn primer:

* symbol - alphanumeric and special symbols  +-@ (can be namespaced)
* :keyword - starts with `:`, used to be a key in a map (can be namespaced)
* "string"
* number
* map
* vector
* set
* list

models are grouped into namespaces, map of model names (keys) and model's data (values).

Each namespace should have a name - `ns` key.

Each model in namespace can be references by full name `<ns-name>/<model-key>`

namespace can `import` another namespace. `import` key is a set of other namespace's names

symbols in zen are reference to other models.
zen checks that referenced name exists.

Local refs can by unnamespaced names (current namespace name is assumed)
Symbols from other namespaces are referenced with full name - `<ns-name>/<model-name>`

## Namespaces & references

Referable unit is **namespace**.  Namespace is a map with symbolic keys.

There are two special keys:

* `ns` defines name of namespace
* `import` is set of namespaces current namespace depends on

Other keys are names of **models** or **symbols**, which are just arbitrary data in edn

```edn
;; file name: my-package/my-namespace.edn
{
  ;; namespace name
  ns my-package.my-namespace

  ;; dependencies
  imports #{library.utils}
  
  ;; model
  model { 
   ;; ...
  }
  
  ;; other model
  other-model {
   ;; ...
  }

}

```

Zen project contains one or more edn files with namespaces.
By convention path to file should match namespace name:
i.e.   `my/package/model.edn` <-> `my.package.model**

One file should contain only one namespace!

Symbols in zen models are interpreted as references to other models.

To reference symbol in same namespace simple name may be used, zen checker
will interpret it as `<current-namespace-name>/<symbol-name>`.

To reference symbol from other namespace, full name should be used `<other-namespace>/<symbol-name>`.
Zen load should check, that referenced namespace is imported and referenced symbol exists in it.

No cyclic references are allowed between namespaces.

Example:

```edn
{ 
  ns myapp
  imports #{zenbox} ;; imported namespace 
  
  operation { 
   ;; ...
  }

  routes {
    :zen/tags #{zenbox/routes} ;; extrnal ref
    :GET operation ;; local ref, i.e. myapp/operation
  }

  main {
    :zen/tags #{zenbox/server} ;; extrnal ref
    :port 8080
    :routes routes ;; local ref , i.e. myapp/routes
  }

}

```

## :zen/tags

Symbol may contain any data in edn format.
Each symbol may be tagged with multiple **tags** - references to other models.
If data tagged with instances of zen/schema, zen loader will validate data against it

Zen runtime should provide an procedure to load all symbols with specific tag!

## zen/schema

Zen is coming with built-in schema language - `zen/schema`,
to describe shape of data in an open way.

zen/schema is a map, where each key is validation instruction.
zen/schema specification is just keys interpretation.

There are few "untyped" keys

* :confirms - set of references to other schemas
* :enum - enumeration of possible values
* :const - fixed value
* :type - edn type of data

Other keys are enabled only for specific "types".

## :confirms

`:confirms` is a set of symbols, referencing other schemas

zen/schema engine should validate current schema and all referenced schemas.

`:confirms`

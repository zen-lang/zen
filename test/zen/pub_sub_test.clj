(ns zen.pub-sub-test
  (:require
   [zen.core :as zen]
   [matcho.core :as matcho]
   [clojure.test :as t]))


(defmethod zen/op 'my/log
  [ztx cfg {ev :ev params :params} & [session]]
  (zen/set-state ztx :test/log params)
  (println :log ev params))

(defmethod zen/op
  'my/react
  [ztx cfg {params :params} & [session]]
  (zen/set-state ztx :test/react-res params))

(defmethod zen/op
  'my/inline-sub
  [ztx cfg {ev :ev params :params} & [session]]
  (zen/set-state ztx :test/inline-res params))

(t/deftest test-pub-sub

  (def ztx (zen/new-context {}))

  (zen/load-ns
   ztx '{ns my

         event
         {:zen/tags #{zen/event}}

         ivent
         {:zen/tags #{zen/event}}

         react
         {:zen/tags #{zen/op}}

         sub
         {:zen/tags #{zen/sub}
          :events #{event}
          :op react}

         inline-sub
         {:zen/tags #{zen/sub}
          :events #{event ivent}}

         log
         {:zen/tags #{zen/sub}
          :events #{zen/all-events}
          :ignore-events #{ivent}}})

  (t/is (empty? (zen/errors ztx)))

  (zen/get-index ztx 'zen/sub 'my/event)

  (:zen/index @ztx)

  (into (or (zen/get-index ztx 'zen/sub 'my/event) #{})
        (zen/get-index ztx 'zen/sub 'zen/all-events))

  (zen/pub ztx 'my/event {:hello 1})
  (zen/pub ztx 'annonimous {})

  (t/is (= {:hello 1} (zen/get-state ztx :test/inline-res)))
  (t/is (= {:hello 1} (zen/get-state ztx :test/react-res)))

  (zen/pub ztx 'annonimous nil)

  (zen/pub ztx 'my/ivent {:ups 1})
  (t/is (nil? (zen/get-state ztx :test/log)))
  (t/is (= {:ups 1} (zen/get-state ztx :test/inline-res)))
  (t/is (= {:hello 1} (zen/get-state ztx :test/react-res)))

  )

(ns zen.utils-test
  (:require [clojure.test :as t]
            [zen.utils :as sut]))


(t/deftest mk-symbol-test
  (t/is (= 'foo/baz (sut/mk-symbol 'foo 'baz)))

  (t/is (= 'foo/baz (sut/mk-symbol 'foo/bar 'baz)))

  (t/is (= 'foo/baz (sut/mk-symbol 'foo/bar 'quux/baz))))


(t/deftest disj-set-test

  (def acc-test-cases
    [{:args   [1 2]
      :assert {1 {:root 1, :group #{1 2}}
               2 {:root 1, :group #{1 2}}}}

     {:args   [:a]
      :assert {1  {:root 1, :group #{1 2}}
               2  {:root 1, :group #{1 2}}
               :a {:root :a, :group #{:a}}}}

     {:args   [:b]
      :assert {1  {:root 1, :group #{1 2}}
               2  {:root 1, :group #{1 2}}
               :a {:root :a, :group #{:a}}
               :b {:root :b, :group #{:b}}}}

     {:args   [:a :b]
      :assert {1  {:root 1, :group #{1 2}}
               2  {:root 1, :group #{1 2}}
               :a {:root :a, :group #{:a :b}}
               :b {:root :a, :group #{:a :b}}}}

     {:args   [:c :b]
      :assert {1  {:root 1, :group #{1 2}}
               2  {:root 1, :group #{1 2}}
               :a {:root :a, :group #{:a :b :c}}
               :b {:root :a, :group #{:a :b :c}}
               :c {:root :a, :group #{:a :b :c}}}}

     {:args   [:d :a]
      :assert {1  {:root 1, :group #{1 2}}
               2  {:root 1, :group #{1 2}}
               :a {:root :a, :group #{:a :b :c :d}}
               :b {:root :a, :group #{:a :b :c :d}}
               :c {:root :a, :group #{:a :b :c :d}}
               :d {:root :a, :group #{:a :b :c :d}}}}])

  (t/is (= (map :assert acc-test-cases)
           (rest
             (reductions
               (fn [s args] (apply sut/disj-set-union-push s args))
               {}
               (map :args acc-test-cases))))))

(ns zen.utils-test
  (:require [clojure.test :as t]
            [zen.utils :as sut]))


(t/deftest disj-set-test

  (def acc-test-cases
    [{:args   [1 2]
      :assert {1 #{1 2}
               2 #{1 2}}}

     {:args   [:a]
      :assert {1  #{1 2}
               2  #{1 2}
               :a #{:a}}}

     {:args   [:b]
      :assert {1  #{1 2}
               2  #{1 2}
               :a #{:a}
               :b #{:b}}}

     {:args   [:b :a]
      :assert {1  #{1 2}
               2  #{1 2}
               :a #{:a :b}
               :b #{:a :b}}}

     {:args   [:c :b]
      :assert {1  #{1 2}
               2  #{1 2}
               :a #{:a :b :c}
               :b #{:a :b :c}
               :c #{:a :b :c}}}

     {:args   [:d :a]
      :assert {1  #{1 2}
               2  #{1 2}
               :a #{:a :b :c :d}
               :b #{:a :b :c :d}
               :c #{:a :b :c :d}
               :d #{:a :b :c :d}}}])

  (t/is (= (map :assert acc-test-cases)
           (rest
             (reductions
               (fn [s args] (apply sut/disj-set-union-push s args))
               {}
               (map :args acc-test-cases))))))

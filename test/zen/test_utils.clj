(ns zen.test-utils
  (:require [matcho.core :as matcho]
            [clojure.test :refer [is]]))

(defmacro vmatch [tctx schemas subj res]
  `(let [res# (zen.core/validate ~tctx ~schemas ~subj)]
     (matcho/match res# ~res)
     res#))

(defmacro match [tctx schema subj res]
  `(let [res# (zen.core/validate ~tctx #{~schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))

(defmacro valid [tctx schema subj]
  `(let [res# (zen.core/validate ~tctx #{~schema} ~subj)]
     (is (empty? (:errors res#)))
     (:errors res#)))

(defmacro valid-schema! [tctx subj]
  `(let [res# (zen.core/validate ~tctx #{'zen/schema} ~subj)]
     (is (empty? (:errors res#)))
     res#))

(defmacro invalid-schema [tctx subj res]
  `(let [res# (zen.core/validate ~tctx #{'zen/schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))



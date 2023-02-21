(ns zen.ftr-utils
   (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.walk]))


(defn dissoc-when
  ([pred m k]
   (cond-> m
     (and (contains? m k)
          (pred (get m k)))
     (dissoc k)))
  ([pred m k & ks]
   (reduce (partial dissoc-when pred)
           (dissoc-when pred m k)
           ks)))


(defn strip-when [pred m]
  (if-let [ks (seq (keys m))]
    (apply dissoc-when pred m ks)
    m))


(defn strip-nils [m]
  (strip-when nil? m))


(defn create-dir-if-not-exists!
  [path]
  (when (seq path)
    (let [file (io/file path)]
      (if (.exists file)
        file
        (do (.mkdirs file)
            file)))))


(defn make-sha256-gzip-writer [output]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        file   (io/file output)]
    {:writer (-> file
                 (java.io.FileOutputStream. true)
                 (java.util.zip.GZIPOutputStream. true)
                 (java.security.DigestOutputStream. digest)
                 (java.io.OutputStreamWriter.)
                 (java.io.BufferedWriter.))
     :file   file
     :digest (fn [] (format "%032x" (BigInteger. 1 (.digest digest))))}))


(defn gzipped-file-content->sha256 [path]
  (let [file   (io/file path)
        digest (java.security.MessageDigest/getInstance "SHA-256")
        digest-stream (-> file
                          (java.io.FileInputStream.)
                          (java.util.zip.GZIPInputStream.)
                          (java.security.DigestInputStream. digest))]
    ;; Reading the whole stream to calculate SHA256 digest
    (while (not= -1 (.read digest-stream)))
    (format "%032x" (BigInteger. 1 (.digest digest)))))


(defn gen-uuid []
  (str (java.util.UUID/randomUUID)))


(defn rmrf [path]
  (let [file (io/file path)]
    (when (.exists file)
      (run! io/delete-file (reverse (file-seq file))))))


(defn parse-ndjson-gz [path]
  (with-open [rdr (-> path
                      (io/input-stream)
                      (java.util.zip.GZIPInputStream.)
                      (io/reader))]
    (->> rdr
         line-seq
         (mapv (fn [json-row]
                 (cheshire.core/parse-string json-row keyword))))))


(defn prepare-map-for-canonical-json-generation [m]
  (clojure.walk/postwalk (fn [node]
                           (if (map? node)
                             (into (sorted-map) node)
                             node))
                         m))


(defn generate-ndjson-row [obj]
  (format "%s\n" (cheshire.core/generate-string (prepare-map-for-canonical-json-generation obj))))


(defn file-exists? [path]
  (when path
    (.exists (io/file path))))


(defn spit-ndjson-gz! [output-path coll]
  (with-open [w (-> output-path
                    (io/file)
                    (java.io.FileOutputStream.)
                    (java.util.zip.GZIPOutputStream. true)
                    (java.io.OutputStreamWriter.)
                    (java.io.BufferedWriter.))]
    (doseq [c coll]
      (.write w (generate-ndjson-row c)))))


(defn move-file! [src dest]
  (java.nio.file.Files/move (.toPath (io/file src))
                            (.toPath (io/file dest))
                            (into-array java.nio.file.CopyOption
                                        [(java.nio.file.StandardCopyOption/ATOMIC_MOVE)
                                         (java.nio.file.StandardCopyOption/REPLACE_EXISTING)])))


(defprotocol NdjsonReader
  (readLine [this] "Reads and parse json line from reader"))


(defn open-ndjson-gz-reader [input]
  (let [ndjson-gz-reader (-> input
                             (io/input-stream)
                             (java.util.zip.GZIPInputStream.)
                             (io/reader))]
    (reify NdjsonReader
      (readLine [this] (-> ndjson-gz-reader
                           .readLine
                           (cheshire.core/parse-string keyword))))))


(defn escape-url [url]
  (some-> url
          str
          (str/replace #"/" "-")
          (str/replace #" " "-")))


(defn open-gz-writer [output]
  (-> output
      (io/file)
      (java.io.FileOutputStream.)
      (java.util.zip.GZIPOutputStream. true)
      (java.io.OutputStreamWriter.)
      (java.io.BufferedWriter.)))


(defn open-gz-reader ^java.io.BufferedReader [input]
  (-> input
      (io/input-stream)
      (java.util.zip.GZIPInputStream.)
      (io/reader)))


(defn psize [path]
  (let [f (io/file path)]
    (if (.isDirectory f)
      (apply + (pmap psize (.listFiles f)))
      (.length f))))


(defn deep-merge
  "efficient deep merge"
  [a b]
  (loop [[[k v :as i] & ks] b
         acc a]
    (if (nil? i)
      acc
      (let [av (get a k)]
        (if (= v av)
          (recur ks acc)
          (recur ks
                 (cond
                   (and (map? v) (map? av)) (assoc acc k (deep-merge av v))
                   (and (set? v) (set? av)) (assoc acc k (into v av))
                   (and (nil? v) (map? av)) (assoc acc k av)
                   :else (assoc acc k v))))))))


(defn flip [f]
  (fn [& args]
    (apply f (reverse args))))

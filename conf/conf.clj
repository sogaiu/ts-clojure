(ns conf
  (:require [babashka.fs :as fs]))

(def verbose
  (System/getenv "VERBOSE"))

(def proj-root (fs/cwd))

;; tree-sitter-clojure

;; if ts-clojure is a subdir of tree-sitter-clojure
(def grammar-dir 
  (->> (fs/absolutize "..")
       fs/normalize
       (format "%s")))
;; if tree-sitter-clojure is a subdir of ts-clojure
;(def grammar-dir "tree-sitter-clojure")

;; tree-sitter

(def ts-bin-path "tree-sitter")

;; ABI   CLI ver  Date
;; ---   -------  ----
;; 14    0.24.0   2024-10
;; 14    0.24.1   2024-10
;; 14    0.24.2   2024-10
;; 14    0.24.3   2024-10
;; 14    0.24.4   2024-11
;; 14    0.24.5   2024-12
;; 14    0.24.6   2024-12
;; 14    0.24.7   2025-01
;; 15    0.25.0   2025-02
;; 15    0.25.1   2025-02
;; 15    0.25.2   2025-02
;; 15    0.25.3   2025-03
;; 15    0.25.4   2025-05

(def abi 14)

;; clojars

(def clojars
  {:name "clojars"
   :root (str proj-root "/clojars-samples/data/clojars-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/clojars-error-files.txt")
   :error-tsv-path (str proj-root "/data/classify-parse-errors.tsv")})

;; clojuredart

(def clojuredart
  {:name "clojuredart"
   :root (str proj-root "/clojuredart-samples/data/clojuredart-repos")
   :extensions #{"clj" "cljc" "cljd"
                 "edn"}
   :error-file-paths (str proj-root "/data/clojuredart-error-files.txt")})

;; core_regression

(def core-regression
  {:name "core-regression"
   :root (str proj-root "/core-regression-samples/data/core-regression-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/core-regression-error-files.txt")})

;; test.regression

(def test-regression
  {:name "test-regression"
   :root (str proj-root "/test-regression-samples/data/test-regression-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/test-regression-error-files.txt")})

;; current repos setting

(def ^:dynamic repos
  #_ clojuredart
  core-regression
  #_ test-regression
  #_ clojars)


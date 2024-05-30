(ns check-setup
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [conf :as cnf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def all-ok? (atom true))

(def count-samples? (atom true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn which
  [name]
  ;; XXX: probably a better way...
  (format "%s" (fs/which name)))

(def prereq-paths
  {:tree-sitter (or (which cnf/ts-bin-path) "*Not Found*")
   :git (or (which "git") "*Not Found*")
   :cc (or (which "cc") "*Not Found*")
   :node (or (which "node") "*Not Found*")})

(defn absolute-path
  [path]
  ;; XXX: probably a better way...
  (format "%s" (fs/absolutize (fs/normalize (fs/file path)))))

;; sample output from tree-sitter dump-languages
;;
;;   scope: source.janet
;;   parser: "./tree-sitter-janet-simple/"
;;   highlights: None
;;   file_types: ["cgen", "janet", "jdn"]
;;   content_regex: None
;;   injection_regex: None
;;
;;   scope: source.clojure
;;   parser: "./tree-sitter-clojure/"
;;   highlights: None
;;   file_types: ["bb", "clj", "cljc", "cljs"]
;;   content_regex: None
;;   injection_regex: None
;;
(defn parsers-from-dump-languages
  []
  (let [out-file-path (fs/create-temp-file)
        _ (fs/delete-on-exit out-file-path)
        p (proc/process {:out :write
                         :out-file (fs/file out-file-path)}
                        (str cnf/ts-bin-path " dump-languages"))
        exit-code (:exit @p)]
    (if-not (zero? exit-code)
      (do
        (reset! all-ok? false)
        nil)
      (keep (fn [line]
              (when (pos? (count line))
                (let [[name value] (cs/split line #": ")]
                  (when (= name "parser")
                    (let [no-quotes (subs value 1 (dec (count value)))]
                      no-quotes)))))
            (fs/read-all-lines (fs/file out-file-path))))))

(defn count-samples
  []
  (let [samples (atom [])]
    (fs/walk-file-tree
     (cnf/repos :root)
     {:visit-file
      (fn [path _]
        (when ((cnf/repos :extensions) (fs/extension path))
          (swap! samples conj path))
        :continue)
      :follow-links true})
    (count @samples)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn print-separator
  []
  (println "------------------------------------------------------------------"))

(defn report-prereq-paths
  []
  (println "tree-sitter:" (get prereq-paths :tree-sitter))
  (println "        git:" (get prereq-paths :git))
  (println "         cc:" (get prereq-paths :cc))
  (println "       node:" (get prereq-paths :node)))

(defn report-grammar-dir
  []
  (let [exists (fs/exists? cnf/grammar-dir)]
    (println "           grammar-dir set to:" cnf/grammar-dir)
    (when-not exists (reset! all-ok? false))
    (println "           grammar-dir exists:"
             (if exists "Yes" "*No*")))
  (let [parsers (parsers-from-dump-languages)]
    ;; XXX: probably there's a better way to do this
    (let [found (atom false)]
      (doseq [p parsers]
        (when (= (absolute-path p)
                 (absolute-path cnf/grammar-dir))
          (reset! found true)))
      (when-not @found (reset! all-ok? false))
      (println "tree-sitter found grammar-dir:"
               (if @found "Yes" "*No*")))))

(defn report-abi
  []
  (println "abi:" cnf/abi))

(defn report-repos
  []
  (let [exists (fs/exists? (cnf/repos :root))]
    (println "         test repos:" (cnf/repos :name))
    (when-not exists (reset! all-ok? false))
    (println "samples root set to:" (cnf/repos :root))
    (println "samples root exists:"
             (if exists "Yes" "*No*"))
    (when (and exists
               @count-samples?)
      (println "       # of samples:" (count-samples)))))

(defn report-exception-and-exit
  [e]
  (println "Exception:" e)
  (System/exit 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; basic outline:
;;
;; * report paths of tree-sitter, git, c compiler, and node
;; * report path of tree-sitter-clojure found by tree-sitter
;; * report path of grammar-dir from conf (should match above)
;; * report abi number from conf
;; * report current repos setting
;; * report paths of samples for current grammar
(defn -main
  [& _args]
  (println "ts-clojure: checking setup")
  (print-separator)
  ;;
  (if (= "-1" (first *command-line-args*))
    (do
      (println "Not counting samples.")
      (reset! count-samples? false))
    (do
      (println "Counting samples too...this might take a while.")
      (println "Hint: Invoke with -1 as argument to skip sample counting.")))
  (print-separator)
  ;;
  (try
    (let [_ (report-prereq-paths)
          _ (print-separator)
          _ (report-grammar-dir)
          _ (print-separator)
          _ (report-abi)
          _ (print-separator)
          _ (report-repos)
          _ (print-separator)]
      (if @all-ok?
        (println "Setup looks ok.")
        (println "*Something isn't right, please review the output*")))
    (catch Exception e
      (report-exception-and-exit e))))


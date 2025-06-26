(ns extract-jars
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.java.io :as io]
            [conf :as cnf])
  (:import [java.io File InputStream]
           [java.net URI]
           [java.nio.file CopyOption StandardCopyOption
            Files
            LinkOption Path]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]
           [java.util.zip ZipInputStream ZipEntry]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; adapted from babashka.fs

(defn- as-path
  ^Path [path]
  (if (instance? Path path) path
      (if (instance? URI path)
        (java.nio.file.Paths/get ^URI path)
        (.toPath (io/file path)))))

(defn- ->copy-opts ^"[Ljava.nio.file.CopyOption;"
  [replace-existing copy-attributes atomic-move nofollow-links]
  (into-array CopyOption
              (cond-> []
                replace-existing (conj StandardCopyOption/REPLACE_EXISTING)
                copy-attributes  (conj StandardCopyOption/COPY_ATTRIBUTES)
                atomic-move      (conj StandardCopyOption/ATOMIC_MOVE)
                nofollow-links   (conj LinkOption/NOFOLLOW_LINKS))))

(defn str->posix
  "Converts a string to a set of PosixFilePermission.

  `s` is a string like `\"rwx------\"`."
  [s]
  (PosixFilePermissions/fromString s))

(defn- ->posix-file-permissions [s]
  (cond (string? s)
        (str->posix s)
        ;; (set? s)
        ;; (into #{} (map keyword->posix-file-permission) s)
        :else
        s))

(defn- posix->file-attribute [x]
  (PosixFilePermissions/asFileAttribute x))

(defn- posix->attrs
  ^"[Ljava.nio.file.attribute.FileAttribute;" [posix-file-permissions]
  (let [attrs (if posix-file-permissions
                (-> posix-file-permissions
                    (->posix-file-permissions)
                    (posix->file-attribute)
                    vector)
                [])]
    (into-array FileAttribute attrs)))

(defn create-dirs
  "Creates directories using `Files#createDirectories`. Also creates parents if needed.
  Doesn't throw an exception if the dirs exist already. Similar to `mkdir -p`"
  ([path] (create-dirs path nil))
  ([path {:keys [:posix-file-permissions]}]
   (Files/createDirectories (as-path path) (posix->attrs posix-file-permissions))))

(defn parent
  "Returns parent of f. Akin to `dirname` in bash."
  [f]
  (.getParent (as-path f)))

(defn unzip
  "Unzips `zip-file` to `dest` directory (default `\".\"`).

   Options:
   * `:replace-existing` - `true` / `false`: overwrite existing files
   * `:satisfies` - function expecting ZipEntry for file, return true => extract"
  ([zip-file] (unzip zip-file "."))
  ([zip-file dest] (unzip zip-file dest nil))
  ([zip-file dest {:keys [replace-existing satisfies]}]
   (let [output-path (as-path dest)
         _ (create-dirs dest)
         cp-opts (->copy-opts replace-existing nil nil nil)]
     (with-open
       [^InputStream fis
        (if (instance? InputStream zip-file) zip-file
            (Files/newInputStream (as-path zip-file) (into-array java.nio.file.OpenOption [])))
       zis (ZipInputStream. fis)]
       (loop []
         (let [entry (.getNextEntry zis)]
           (when entry
             (let [entry-name (.getName entry)
                   new-path (.resolve output-path entry-name)]
               (if (.isDirectory entry)
                 (create-dirs new-path)
                 (when (or (nil? satisfies)
                           (and (fn? satisfies) (satisfies entry)))
                   (create-dirs (parent new-path))
                   (Files/copy ^java.io.InputStream zis
                               new-path
                               cp-opts))))
             (recur))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn parse-jar-path
  [path]
  (let [regex (re-pattern (str "^"
                               cnf/clojars-jars-root
                               "/(.*)/"
                               "([^/]+\\.jar)"
                               "$"))]
    (when-let [[_ subpath jar-name] (re-matches regex path)]
      [subpath jar-name])))

(defn -main
  [& _args]
  (when (not (fs/exists? cnf/clojars-jars-root))
    (fs/create-dir cnf/clojars-jars-root))
  (when (fs/exists? cnf/clojars-jars-root)
    (let [start-time (System/currentTimeMillis)
          jar-paths (atom [])]
      ;; find all jar files
      (print "Looking for jar files ... ")
      (flush)
      (fs/walk-file-tree cnf/clojars-jars-root
                         {:visit-file
                          (fn [path _]
                            (when (= "jar" (fs/extension path))
                              (swap! jar-paths conj path))
                            :continue)
                          :follow-links true})
      (println "found"
               (count @jar-paths) "jar files"
               "in" (- (System/currentTimeMillis) start-time) "ms")
      ;; unzip jar files
      (print "Unzipping jar files ... ")
      (flush)
      (let [start-time (System/currentTimeMillis)
            counter (atom 0)]
        (doseq [jar-path @jar-paths]
          (when-let [[subpath jar-name] (parse-jar-path (str jar-path))]
            (let [dest-dir (str cnf/clojars-repos-root "/" subpath)]
              (when-not (fs/exists? dest-dir)
                (fs/create-dirs dest-dir)
                (try
                  (unzip (fs/file jar-path) dest-dir
                         ;; XXX: better than nothing but not ideal
                         {:replace-existing true
                          :satisfies (fn [entry]
                                       (let [path (.getName entry)
                                             ext (fs/extension path)]
                                         ;; e.g. (#{"bb" "clj"} "txt")
                                         (cnf/extensions ext)))})
                  (swap! counter inc)
                  (catch Exception e
                    (fs/delete-tree dest-dir)
                    (println "Problem unzipping jar:" jar-path)
                    #_(println "Exception:" e)))))))
        ;; report summary
        (println "took" (- (System/currentTimeMillis) start-time) "ms"
                 "to unzip" @counter "jar files")))))


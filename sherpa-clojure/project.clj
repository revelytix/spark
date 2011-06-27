(def version "0.1.3-SNAPSHOT")

(defproject com.revelytix/sparql-clojure version
  :description "Defines a SPARQL client and server processing framework using the SHERPA protocol."

  ;; Project dependencies, resolved via Maven
  ;; Format is: [<groupId>/<artifactId> "version"]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]

                 ;; revelytix projects
                 [com.revelytix/sherpa-protocol version]
                 [com.revelytix/sherpa-java version]]

  :dev-dependencies [[jline "0.9.94"]
                     [swank-clojure "1.2.1"]
                     [autodoc "0.8.0-RAS"]]

  ;; Use a Maven-like directory structure
  :source-path "src/main/clojure"
  :test-path "src/test/clojure"
  :resources-path "src/main/resources"
  :dev-resources-path "src/test/resources"
  :target-dir "target"
  :compile-path "target/classes"

  :omit-default-repositories true

  :repositories {"rev-repo" {:url "https://build.knoodel.net/maven-repo/content/groups/public"
                             :username "revelytix-deploy"
                             :password "f9uUejDk"}})

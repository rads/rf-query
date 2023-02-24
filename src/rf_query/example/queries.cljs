(ns rf-query.example.queries
  (:require [rf-query.core :as rq]
            ["@tanstack/react-query" :refer [useQuery QueryClient
                                             QueryClientProvider]]))

(def query-client (QueryClient.))

(rq/set-config! {:use-query-fn useQuery})

(defn provider [& children]
  (into [:> QueryClientProvider {:client query-client}]
        children))

(def counter
  {:query-key ["counter"]
   :query-fn (fn []
               (js/Promise.
                 (fn [resolve]
                   (js/setTimeout
                     (fn [] (resolve (rand-int 100)))
                     3000))))})

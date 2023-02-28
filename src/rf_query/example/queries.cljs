(ns rf-query.example.queries
  (:require [rf-query.core :as rq]
            ["@tanstack/react-query" :refer [useQuery
                                             useMutation
                                             QueryClient
                                             QueryClientProvider]]))

(def query-client (QueryClient.))

(rq/set-config! {:use-query-fn useQuery
                 :use-mutation-fn useMutation})
(println @rq/current-config)

(defn provider [props & children]
  [:> QueryClientProvider {:client query-client}
   (into [rq/provider props]
         children)])

(def counter
  {:query-key ["counter"]
   :query-fn (fn []
               (js/Promise.
                 (fn [resolve]
                   (js/setTimeout
                     (fn [] (resolve (rand-int 100)))
                     3000))))})

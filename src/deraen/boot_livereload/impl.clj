(ns deraen.boot-livereload.impl
  "From https://github.com/bhurlow/clj-livereload/blob/master/src/clj_livereload/core.clj"
  (:require [org.httpkit.server :refer [run-server with-channel on-close on-receive send! open?]]
            [ring.util.response :as resp]
            [ring.middleware.resource :as mwres]
            [cheshire.core :as json]))

(defn- hello-message []
  {:command "hello"
   ; Tiny-lr supports only this, so I think it's safe to do the same
   :protocols ["http://livereload.com/protocols/official-7"]
   :serverName "boot-livereload"})

(defn send-reload-msg
  "Given state and path, send reload message to the clients."
  [state path]
  (doseq [channel (:reload-channels @state)]
    (if (open? channel)
      (send! channel
             (json/generate-string
               {:command "reload"
                :path (str "/" path)
                :liveCSS true}))
      (swap! state update-in [:reload-channels disj channel]))))

(defn- handle-livereload [state req]
  (with-channel req channel
    (swap! state update-in [:reload-channels] conj channel)
    (on-receive
      channel
      (fn [data]
        (let [parsed (json/decode data true)]
          (case (:command parsed)
            "hello" (send! channel (json/generate-string (hello-message)))
            nil))))
    (on-close
      channel
      (fn [_]
        (swap! state update-in [:reload-channels] disj channel)))))

(defn- wrap-livereload [handler state]
  (fn [req]
    (if (= :get (:request-method req))
      (case (:uri req)
        "/livereload.js" (-> (resp/resource-response "META-INF/resources/webjars/livereload-js/2.2.2/dist/livereload.js" {:root ""})
                             (resp/content-type "application/javascript"))
        "/livereload" (handle-livereload state req)
        (handler req)))))

(defn start
  [{:keys [port asset-path]}]
  (let [state (atom {:reload-channels #{}})
        http-kit (run-server (-> (fn [req] (mwres/resource-request req asset-path))
                                 (wrap-livereload state))
                             {:port port})]
    {:state state
     :http-kit http-kit
     :port (:local-port (meta http-kit))}))

(defn stop
  "Stop the http-server and watch service."
  [server]
  (if-let [http-kit (:http-kit server)]
    (http-kit :timeout 100))
  nil)

(def server (atom nil))

(defn start!  [opts]
   (select-keys (swap! server (fn [server] (or server (start opts)))) [:port]))

(defn stop! []
  (swap! server (fn [server] (when server (stop server)))))

(defn send-reload-msg! [path]
  (if-let [state (:state @server)]
    (send-reload-msg state path)))

(ns api-programacao-funcional.core
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test])
  )

(println "Started API")

(def store (atom {}))
;{id {projeto_id projeto_nome projeto_status} }

(defn lista-projetos [request]
  {:status 200 :body @store})

(defn criar-projeto-mapa [uuid nome status]
  {:id uuid :nome nome :status status})

(defn criar-projeto [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :nome])
        status (get-in request [:query-params :status])
        projeto (criar-projeto-mapa uuid nome status)]
    (swap! store assoc uuid projeto)
    (:status 200 :body {:mensagem "Projeto registrado com sucesso!"
                        :projeto   projeto})
    ))

(defn funcao-hello [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody!"))})

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world]
                ["/projeto" :post criar-projeto :route-name :criar-projeto]
                ["/projeto" :get lista-projetos :route-name :lista-projetos]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))

(defn start-server []
  (reset! server (http/start (http/create-server service-map))))


(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(println (test-request :get "/hello?name=Jessica"))
(println (test-request :post "/projeto?nome=Projeto1&status=pendente"))
(println (test-request :post "/projeto?nome=Projeto2&status=finalizado"))
(println (test-request :post "/projeto?nome=Projeto3&status=atualizado"))
(println (test-request :post "/projeto?nome=Projeto4&status=finalizado"))

(println "Listando todos os projetos")
(println (test-request :get "/projeto"))
(test-request :get "/projeto")


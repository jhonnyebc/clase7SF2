// Datos publicos de Auth0 y de la API. Ninguno es secreto: una aplicacion Angular
// los expone en el navegador de todas formas.
export const configuracion = {
  apiUrl: 'http://localhost:8081/api/v1',
  auth0: {
    domain: 'dev-x3tskzc0kemi5tlv.us.auth0.com',
    clientId: '6aPiRY6X0jsWQ9zeRfTq9tFizSuKBp8E',
    audience: 'https://uco-test-api',
  },
};

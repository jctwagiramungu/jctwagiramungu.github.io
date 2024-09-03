  <script language="JavaScript" type="text/javascript"
          src="https://kjur.github.io/jsrsasign/jsrsasign-latest-all-min.js">
  </script>

  <script>

const PUBLIC_KEY = 'Q29va2llIEJhbm5lciB1c2luZyBPbmVUcnVzdA==';
  // Header
var oHeader = {alg: 'HS256', typ: 'JWT'};
// Payload
var oPayload = {};
var tNow = KJUR.jws.IntDate.get('now');
var tEnd = KJUR.jws.IntDate.get('now + 1day');
//oPayload.iss = "http://foo.com";
oPayload.sub = "jeanclaude.twagiramungu70078@onetrust.com";
oPayload.nbf = tNow;
oPayload.iat = tNow;
oPayload.exp = tEnd;
//oPayload.jti = "id123456";
//oPayload.aud = "http://foo.com/employee";
// Sign JWT, password=616161
var sHeader = JSON.stringify(oHeader);
var sPayload = JSON.stringify(oPayload);
var sJWT = KJUR.jws.JWS.sign("HS256", sHeader, sPayload, PUBLIC_KEY);

console.log(sJWT);
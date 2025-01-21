docker build -t cds-hooks-ice-wrapper 
docker run -p 8081:8081 --name cds-hooks-ice-wrapper-container cds-hooks-ice-wrapper
нужно запустить ice движок
docker pull hlnconsulting/ice
docker run -p 8080:8080 hlnconsulting/ice

тестовый 
curl -X GET http://localhost:8081/cds-services


тестовый пациент с Fihr
curl -X GET "https://au-core.beda.software/fhir/Patient/patient-tc-2" -H "Accept: application/json"



для этого же пациента данные о вакцинации 
curl -X GET "https://au-core.beda.software/fhir/Immunization?patient=patient-tc-2" -H "Accept: application/json"


Получение всех пациентов 
curl -X GET "https://au-core.beda.software/fhir/Patient" -H "Accept: application/json"




Настройки приложения:

spring.application.name=cds-hooks-ice-wrapper  
server.address=0.0.0.0  
server.port=8081  
fhir.server.url=https://au-core.beda.software/fhir/Patient  
ice.service.url=http://localhost:8080/opencds-decision-support-service/version










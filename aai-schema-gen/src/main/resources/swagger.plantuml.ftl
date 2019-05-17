@startuml

title ${version} Active and Available Inventory Relationships Class Diagram


class Inventory
<#list sortedAaiApis?keys as key>
	<#list sortedAaiApis[key] as api>
class ${api.getPath()}
	</#list>
</#list>

"Inventory" *-- "business"
"Inventory" *-- "cloud-infrastructure"
"Inventory" *-- "common"
"Inventory" *-- "external-system"
"Inventory" *-- "network"
"Inventory" *-- "service-design-and-creation"

Note: Convert the paths below into compositions like above
<#list sortedAaiApis?keys as key>
	<#list sortedAaiApis[key] as api>
${api.getPath()}
	</#list>
</#list>

@enduml


#!/bin/sh

# time ./generate-uml-snapshot.sh 2>&1 | tee log-generate-uml-snapshot.txt

set -x

# start in aai/schema-service repo
STARTDIR=`pwd`
git status

# modify templates
if [ -f ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.html.ftl -a -f ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.plantuml.ftl ] ; then
  cp ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.plantuml.ftl ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.html.ftl
  mvn --offline -DskipTests process-classes
  git status
fi

# $ find aai-schema/src/main -name "aai_swagger_*.html"
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v10.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v11.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v12.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v13.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v14.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v15.html
# aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_v16.html


mkdir -p ${STARTDIR}/plantuml/backups
cp ${STARTDIR}/aai-schema/src/main/resources/onap/aai_swagger_html/aai_swagger_*.html ${STARTDIR}/plantuml/

cd ${STARTDIR}/plantuml/
 
# OXM transformation - class names
for f in aai_swagger_*.html ; do mv $f `basename $f | sed 's/\.html//'`.plantuml ; done

# pre-clean
sed -i.bak0 -s 's/\/{[^\/]*}//g' aai_swagger_*.plantuml
sed -i.bak1 -s 's/\/relationship[^\/]*//g' aai_swagger_*.plantuml

grep -c "^class.*\/" aai_swagger_*.plantuml
RET=$?
COUNT=0
while [ ${RET} -eq 0 ] ; do
  COUNT=$(( COUNT + 1 )) 
  echo Replacing ${COUNT}...
  sed -i.bak2-${COUNT} -s 's/^class \/\([^\/]*\)/class \1\nclass /' aai_swagger_*.plantuml
  grep -c "^class.*\/" aai_swagger_*.plantuml
  RET=$?
done

# unique without sorting
for f in aai_swagger_*.plantuml ; do
  cp ${f} ${f}.bak3
  awk '!a[$0]++' ${f}.bak3 > ${f}
done

# OXM transformation - compositions
sed -i.bak4 -s 's/^\//"/g' aai_swagger_*.plantuml

grep -c "^\".*\/.*\/" aai_swagger_*.plantuml
RET=$?
COUNT=0
while [ ${RET} -eq 0 ] ; do
  COUNT=$(( COUNT + 1 )) 
  echo Replacing ${COUNT}...
  sed -i.bak5-${COUNT} -s 's/^"[^\/]*\/\([^\/]*\)\//"\1\//' aai_swagger_*.plantuml
  grep -c "^\".*\/.*\/" aai_swagger_*.plantuml
  RET=$?
done

sed -i.bak6 -s 's/^"\([^\/]*\)\/\([^\/]*\)/"\1" *-- "\2"/' aai_swagger_*.plantuml

# post-clean
sed -i.bak7 -s '/^class $/d' aai_swagger_*.plantuml
sed -i.bak8 -s '/^Note: Convert/d' aai_swagger_*.plantuml

# unique without sorting
for f in aai_swagger_*.plantuml ; do
  cp ${f} ${f}.bak9
  awk '!a[$0]++' ${f}.bak9 > ${f}
done


# $ find aai-schema/src/main -name "DbEdgeRules*.json"
# aai-schema/src/main/resources/onap/dbedgerules/v10/DbEdgeRules_v10.json
# aai-schema/src/main/resources/onap/dbedgerules/v11/DbEdgeRules_esr_v11.json
# aai-schema/src/main/resources/onap/dbedgerules/v11/DbEdgeRules_v11.json
# aai-schema/src/main/resources/onap/dbedgerules/v12/DbEdgeRules_esr_v12.json
# aai-schema/src/main/resources/onap/dbedgerules/v12/DbEdgeRules_hpa_v12.json
# aai-schema/src/main/resources/onap/dbedgerules/v12/DbEdgeRules_v12.json
# aai-schema/src/main/resources/onap/dbedgerules/v13/DbEdgeRules_esr_v13.json
# aai-schema/src/main/resources/onap/dbedgerules/v13/DbEdgeRules_hpa_v13.json
# aai-schema/src/main/resources/onap/dbedgerules/v13/DbEdgeRules_v13.json
# aai-schema/src/main/resources/onap/dbedgerules/v14/DbEdgeRules_ccvpn_v14.json
# aai-schema/src/main/resources/onap/dbedgerules/v14/DbEdgeRules_esr_v14.json
# aai-schema/src/main/resources/onap/dbedgerules/v14/DbEdgeRules_hpa_v14.json
# aai-schema/src/main/resources/onap/dbedgerules/v14/DbEdgeRules_pnp_v14.json
# aai-schema/src/main/resources/onap/dbedgerules/v14/DbEdgeRules_v14.json
# aai-schema/src/main/resources/onap/dbedgerules/v15/DbEdgeRules_ccvpn_v15.json
# aai-schema/src/main/resources/onap/dbedgerules/v15/DbEdgeRules_esr_v15.json
# aai-schema/src/main/resources/onap/dbedgerules/v15/DbEdgeRules_hpa_v15.json
# aai-schema/src/main/resources/onap/dbedgerules/v15/DbEdgeRules_pnp_v15.json
# aai-schema/src/main/resources/onap/dbedgerules/v15/DbEdgeRules_v15.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_bbs_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_ccvpn_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_esr_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_hpa_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_pnp_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v16/DbEdgeRules_v16.json
# aai-schema/src/main/resources/onap/dbedgerules/v8/DbEdgeRules_v8.json
# aai-schema/src/main/resources/onap/dbedgerules/v9/DbEdgeRules_v9.json

# EdgeRule transformation
for g in v16 v15 v14 v13 v12 v11 ; do
  cat ${STARTDIR}/aai-schema/src/main/resources/onap/dbedgerules/${g}/DbEdgeRules*_${g}.json >> ${STARTDIR}/plantuml/aai_edgerules_${g}.plantuml
done

for f in v16 v15 v14 v13 v12 v11 ; do
  cp aai_edgerules_${f}.plantuml aai_edgerules_${f}.plantuml.bak1
  echo "@startuml" > aai_edgerules_${f}.plantuml
  echo "title ${f} Active and Available Inventory EdgeRule Relationships" >> aai_edgerules_${f}.plantuml
  awk -F \" '/"from"/ { from = $4 } /"to"/ { to = $4 } /"label"/ { label = $4 ; print "\"" from "\" -- \"" to "\" : " label " >" }' aai_edgerules_${f}.plantuml.bak1 | sort -u >> aai_edgerules_${f}.plantuml 
  echo "@enduml" >> aai_edgerules_${f}.plantuml
done

sed -i.bak2 -s '/inventory.BelongsTo/d' aai_edgerules_*.plantuml
sed -i.bak3 -s 's/: \(.*\.\)/: /' aai_edgerules_*.plantuml

# save backups and restore originals
cd ${STARTDIR}
mv ${STARTDIR}/plantuml/*.bak* ${STARTDIR}/plantuml/backups
ls -alR ${STARTDIR}/plantuml
git status

if [ -f ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.html.ftl -a -f ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.plantuml.ftl ] ; then
  git checkout ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.plantuml.ftl ${STARTDIR}/aai-schema-gen/src/main/resources/swagger.html.ftl
  mvn --offline -DskipTests process-classes

  # $ find aai-schema/src/main -name "aai_swagger_*.yaml"
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v10.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v10.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v11.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v11.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v12.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v12.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v13.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v13.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v14.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v14.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v15.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v15.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v16.nodes.yaml
  # aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_v16.yaml

  # convert yaml to json
  for g in v16 v15 v14 v13 v12 v11 ; do
    ${STARTDIR}/yaml2json.py < ${STARTDIR}/aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_${g}.yaml > ${STARTDIR}/plantuml/aai_swagger_${g}.json
    ${STARTDIR}/yaml2json.py < ${STARTDIR}/aai-schema/src/main/resources/onap/aai_swagger_yaml/aai_swagger_${g}.nodes.yaml > ${STARTDIR}/plantuml/aai_swagger_${g}.nodes.json
  done

  git status
fi

ls -al ${STARTDIR}/plantuml


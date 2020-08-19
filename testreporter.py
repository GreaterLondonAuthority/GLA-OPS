
import json
import xml.etree.cElementTree as ET

print "Test reporter"

with open('target/ui/cucumber_report.json') as data_file:    
    data = json.load(data_file)

suites = ET.Element("testsuites")
suite = ET.SubElement(suites, "testsuite", name="Empty", errors="0", tests="1", failures="0", time="0")

cases = 0
failed = 0
for item in data:
	print ">>",item['description']
	case = ET.SubElement(suite, "testcase", classname="IntegrationTest", name=item['description'])
	cases += 1
	allPassed = True
	for assertion in item['assertions']:
		if assertion['passed'] != True:
			allPassed = False
	if not allPassed:
		failed += 1
		ET.SubElement(case, "failure", message="test failed").text = 'Assertion failed'

suite.set('tests',str(cases))
suite.set('failures',str(failed))

tree = ET.ElementTree(suites)
tree.write("target/ui/cucumber-junit.xml",encoding="UTF-8",xml_declaration=True)

print "Done"

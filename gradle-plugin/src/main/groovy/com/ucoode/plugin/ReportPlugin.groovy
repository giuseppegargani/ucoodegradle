package com.ucoode.plugin

import groovy.json.JsonOutput
import org.gradle.api.Plugin
import org.gradle.api.Project

//VARIOUS NOTES AND PREVIOUS CODE

/* BEFORETEST PREVIOUS CODE
def nomeintermedio = descriptor.className       //nome della classe comprensivo di package, subproject e class
                        logger.lifecycle("POSSIBILI PROPERTIES $risultati ${descriptor.properties } ")
                        logger.lifecycle("TEST IN ESECUZIONE:  + ${descriptor.name } e $risultati e $nomeintermedio")
                        //verifica ogni volta il nome della classe e se diverso salva e svuota
                        String[] str;
                        def regex = '[.]'
                        str = nomeintermedio.split(regex);
                        println("lunghezza array "+ str.length )
                        println("primo ${str[0]} ")
                        if(str.length<4){ println ("nome di lunghezza irregolare!! numero ${str.length} segmenti!! verificare per cortesia"); return;} else { println("nome di lunghezza corretta") }
                        println("nome classe: ${str[str.length-1]}")
                        //qui si va ad aggiornare il nome del pacchetto!!!!!
                        nomePacchetto = str[0]+"."+str[1]+"."+str[2]
                        println ("NOME PACCHETTO: $nomePacchetto")
                        def rimosso = nomeintermedio.minus(nomePacchetto+".")
                        println("RIMOSSO $rimosso")
                        if(nomeClasse!=rimosso) {
                            def tempInter = ["test_class_name":nomeClasse,"tests_list":risultati]
                            println("NOME CLASSE DIVERSO E QUINDI REINIZIALIZZATO $nomeClasse --- $risultati --- $tempInter ");
                            intermedi.add(tempInter)
                            //risultati = []
                            nomeClasse=rimosso;
                            println("----------INTERMEDI: $intermedi")
                        } else { println("NOME CLASSE UGUALE e quindi accumula $intermedi") }
 */

/* AFTERTEST PREVIOUS CODE
               ///risultati.put(desc.name, result.toString())
               //defaults.put('f','g')
                def singleTest = ["outcome":result.toString(), "testName":desc.name.toString()]
                //def outcomeTest = ["outcome":result.toString()]
                 risultati.add(singleTest)
                 println("NOME: $singleTest e risultati: $risultati")
                 //risultati.put(desc.name.toString(), result.toString())
                 logger.lifecycle("************ Beppe: NOME TEST: ${desc.name} e RISULTATO TEST: $result")
                 logger.lifecycle("********** BEPPE: risultato fino ad adesso: $risultati")
 */

/* BEFORESUITE PREVIOUS CODE
   //risultati = []   //azzera la variabile
    println("Si mette in ascolto $desc e inizializza l'array $risultati")
    //if(desc.className!=null){println("INIZIATA UNA SUITE ${desc.properties}") }
    //println("INIZIATA UNA SUITE ${desc.properties}")
*/

/* AFTERSUITE PREVIOUS CODE
                        if (!desc.parent) { // will match the outermost suite
                            def output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
                            def startItem = '|  ', endItem = '  |'
                            def repeatLength = startItem.length() + output.length() + endItem.length()
                            println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
                        }*//*
                        println("ELENCO FINALE CON EVENTI: $risultati")
                        println("NOME DELLA CLASSE E PACCHETTO: ${desc.properties}")

                        //DOPO SI POSSONO ANCHE RIMETTERE!!!! ma adesso ci concentriamo su Json strutturato come si vuole
                        //def json = JsonOutput.toJson(risultati)
                        //new File("/ReportUnitTest.json").write(json)
                        //def json = new JsonSlurper().parseText('{"a":"b"}')
                        //if(desc.className!=null){println("TERMINATA UNA SUITE ${desc.properties}") }
                        //println("2TERMINATA UNA SUITE ${result.properties}")
                        //println("TERMINATA UNA SUITE ${desc.properties}")
                        if(desc.displayName.startsWith("Gradle Test Run ")) {println("TERMINATA UNA SUITE ${desc.properties}")}
                        //println(json);

 */

class ReportPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.android {

            testVariants.all { variant ->
                variant.connectedInstrumentTest.doLast {
                    println "The name of the test type: $connectedInstrumentTest.name"
                    println "The type of test $connectedInstrumentTest.class"
                }
                //We should remove hardcoded reference and make it relative to android library!!! to print instrument report json!!
                project.connectedAndroidTest.finalizedBy(':app:ucoodeTest')
                project.assembleDebugAndroidTest.finalizedBy(':app:ucoodeTest')
            }

            testOptions {

                execution 'ANDROIDX_TEST_ORCHESTRATOR'

                unitTests.all {

                    def testResults = []
                    def intermedi = []
                    def finali = []
                    def clssName = ""
                    def pckgName = ""

                    ignoreFailures = true

                    beforeTest { descriptor -> }

                    afterTest { desc, result ->
                        def singleTest = ["outcome":result.toString(), "testName":desc.name.toString()]
                        testResults.add(singleTest)
                    }

                    beforeSuite { desc ->
                        if(desc.displayName.startsWith("Gradle Test Run ")) {println("INIZIATA UNA SUITE ${desc.properties}")}
                    }
                    afterSuite { desc, result ->

                        //questo se termina una classe di test singola (verifica aggiuntiva del numero di test interni)
                        if((desc.className!=null)&&(testResults.size()>0)){

                            //it divides the classname in segments of path and it assigns them
                            String[] pathArray = desc.className.split('[.]')
                            pckgName = pathArray[0]+"."+pathArray[1]+"."+pathArray[2]
                            clssName = (desc.className).minus(pckgName+".")

                            def singleTestClass = ["test_class_name":clssName,"tests_list":testResults]
                            intermedi.add(singleTestClass)
                            testResults = []
                        }
                        //the outer class
                        if(desc.displayName.startsWith("Gradle Test Run ")) {
                            finali = ["package_name":pckgName, "test_classes_list":intermedi]
                            //it saves on json
                            def json = JsonOutput.toJson(finali)
                            new File("ReportUnitTest.json").write(json)
                        }
                        //print the results more clearly
                        if (!desc.parent) { // will match the outermost suite
                            def output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
                            def startItem = '|  ', endItem = '  |'
                            def repeatLength = startItem.length() + output.length() + endItem.length()
                            println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
                        }
                    }

                    //Granularity in console about the results of the unit tests (verified that 3 corresponds to the single test methods)
                    testLogging {
                        minGranularity 3
                        maxGranularity 3
                    }
                    onOutput { descriptor, event ->
                        logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message )
                    }
                }
            }
        }

        /*project.task('hello') {
            doLast {
                println "${extension.message.get()} from ${extension.greeter.get()}"
            }
        }*/
        // Create a task using the task type
        project.task('hello') {
            doLast {
                println("CIAO DALLA CUSTOM TASK")
            }
        }

        //project.tasks.register('hello')

        project.task('ucoodeTest') {
            def nomeapp = 'nome'

            doLast {
                nomeapp = project.android.defaultConfig.applicationId
                println "The package name of the app: $nomeapp"
            }
            doLast {
                try{
                    project.exec {
                        //we could also put in just one line: '&&', 'adb', 'pull', 'sdcard/android/data/com.example.jsonreport/files/JsonTestReport.json'
                        commandLine 'adb', 'shell', 'am', 'instrument', '-w', "${nomeapp}.test/androidx.test.runner.AndroidJUnitRunner"
                    }
                    //println("The PREVIOUS report has been correctly transfered and if you want to update the REPORT please retry this OR MANUALLY PASS THE FILE TO THE ROOT PROJECT FOLDER BY TERMINAL: gradlew passReport")
                    //e legge per verifica
                    //String fileContents = new File('JsonTestReport.json').text
                    //println(fileContents)
                } catch(all){
                    println("THE REPORT HAS NOT YET BEEN TRANSFERED SO PLEASE RETRY OR MANUALLY PASS THE FILE TO THE ROOT PROJECT FOLDER BY TERMINAL: gradlew passReport")
                }
            }
            doLast {
                try{
                    project.exec{
                        commandLine 'adb', 'pull', "sdcard/android/data/${nomeapp}/files/JsonTestReport.json", '../'
                    }
                    println("THE REPORT HAS BEEN TRANSFERED CORRECTLY")
                    String fileContents = new File('JsonTestReport.json').text
                    println(fileContents)
                } catch(all){
                    println("THE REPORT HAS NOT YET BEEN TRANSFERED SO PLEASE RETRY OR MANUALLY PASS THE FILE TO THE ROOT PROJECT FOLDER BY TERMINAL: gradlew passReport")
                }

            }
        }

        //VERIFICARE CHE TRASFERISCA PER DIVERSI COMANDI TEST STRUMENTALE, CON IF CONDIZIONALE (NON ERRORE) E SENZA ANDROIDJUNITRUNNER
        project.task('passReport') {
            def nomeapp = 'nome'

            doLast {
                nomeapp = it.android.defaultConfig.applicationId
                println "The package name is: $nomeapp"
            }
            doLast {
                try{
                    project.exec {
                        //copy the file from emulator to root project folder
                        commandLine 'adb', 'pull', "sdcard/android/data/${nomeapp}/files/JsonTestReport.json", '../'
                    }
                    println("The report has been correctly transfered to the root project folder")
                    //e legge per verifica
                    String fileContents = new File('JsonTestReport.json').text
                    println(fileContents)
                } catch(all){
                    println("SOME PROBLEM HAS OCCURED WITH THE MANUAL TRANSFER OF REPORT: please inform us by email")
                }
            }
        }
    }
}

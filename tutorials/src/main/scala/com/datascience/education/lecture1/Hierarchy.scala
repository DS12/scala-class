package tutorials.lecture3

import scala.language.implicitConversions

object Hierarchy extends App {

  import org.json4s._
  import org.json4s.native.JsonMethods._


  val json1 = """
{
  "firstName": "John",
  "lastName": "Smith",
  "isAlive": true,
  "age": 25,
  "address": {
    "streetAddress": "21 2nd Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10021-3100"
  },
  "phoneNumbers": [
    {
      "type": "home",
      "number": "212 555-1234"
    },
    {
      "type": "office",
      "number": "646 555-4567"
    }
  ],
  "children": [],
  "spouse": null
}
"""


  val json2 = """
{
  "name" : "Bert",
  "children" : [
    {
	  "name" : "Alice",
	  "children" : []
	},
	{ 
	  "name": "Bob",
	  "children" : [
	    {
		  "name" : "Bill",
		  "children" : []
	    },
		{
		  "name" : "Zoot",
		  "children" : []
	    }
	  ]
	}
  ]
}
"""

    // Task (1a)

}


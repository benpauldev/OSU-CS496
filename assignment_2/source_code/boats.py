from google.appengine.ext import ndb
import webapp2
import json
import base64

class Boat(ndb.Model):
	id = ndb.StringProperty(required=True)
	name = ndb.StringProperty(required=True)
	type = ndb.StringProperty(required=True)
	length = ndb.IntegerProperty(required=True)
	at_sea = ndb.BooleanProperty(required=True)
	slip = ndb.StringProperty()

	

class _boats(webapp2.RequestHandler):

	def __init__(self, *args, **kwargs):
		self.err = False
		super(_boats, self).__init__(*args, **kwargs)

	def error_handler(self, errorNo, errorMsg):
		self.response.status = errorNo
		self.response.write(errorMsg)
		self.err = True

	def post(self):

		# Exception Handling: https://docs.python.org/3/tutorial/errors.html
		try:
			boat_data = json.loads(self.request.body)
		except ValueError:
			self.error_handler(405,'Error: Invalid JSON in request body.')

		if not self.err:	
			
			boat_data['at_sea'] = True
			new_boat = Boat(**boat_data)
			new_boat.put()
			boat_dict = new_boat.to_dict()
			boat_dict['self'] = '/boats/' + new_boat.key.urlsafe()
			self.response.write(json.dumps(boat_dict))


	def get(self, id=None):
		if id:
			try:
				boat = ndb.Key(urlsafe=id).get()
			except ValueError:
				self.error_handler(405,'Error: No such boat by that ID.')
			if boat:
				b_d = boat.to_dict()
				b_d['self'] = '/boats/' + id
				self.response.write(json.dumps(b_d))
			else:
				self.error_handler(405,"Error: Invalid Boat ID.")
		else:
			boats = Boat.query().fetch()
			b_ds = {'Boats':[]}
			for boat in boats:
				id = boat.key.urlsafe()
				boat_data=boat.to_dict()
				boat_data['self'] = '/boats/' + id
				boat_data['id'] = id
				b_ds['Boats'].append(boat_data)
			self.response.write(json.dumps(b_ds))

	def delete(self, id = None):
		if id:
			boat = ndb.Key(urlsafe=id).get()
			if boat:
				if boat.at_sea == False:
					slip = ndb.Key(urlsafe=boat.slip).get()
					slip.arrival_date = 'null'
					slip.current_boat = 'null'
					slip.put()
				boat.key.delete()
				self.response.write('Successfully Deleted Boat:' + id)
			else:
				self.error_handler(405,"Error: Invalid Boat ID.")
		else:
			boats = Boat.query().fetch()
			for boat in boats:
				boat.key.delete()


	def patch(self, id = None):
		if id:
			boat = ndb.Key(urlsafe=id).get()
			if boat:
				b_d = json.loads(self.request.body)
				if 'name' in b_d:
					boat.name = b_d['name']
				if 'length' in b_d:
					boat.length = b_d['length']
				if 'type' in b_d:
					boat.type = b_d['type']
				boat.put()
				b_dict = boat.to_dict()
				self.response.write(json.dumps(b_dict))
			else:
				self.error_handler(405, "Error: Invalid boat id.")
		else:
			self.error_handler(403, "Error: No ID provided.")























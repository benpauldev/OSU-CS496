from google.appengine.ext import ndb
import webapp2
import json
import base64
from boats import Boat, _boats

class Slip(ndb.Model):
	id = ndb.StringProperty(required=True)
	number = ndb.IntegerProperty(required=True)
	current_boat = ndb.StringProperty(required=True)
	arrival_date = ndb.StringProperty(required=True)

	
class _slips(webapp2.RequestHandler):

	def __init__(self, *args, **kwargs):
		self.err = False
		super(_slips, self).__init__(*args, **kwargs)

	def error_handler(self, errorNo, errorMsg):
		self.response.status = errorNo
		self.response.write(errorMsg)
		self.err = True


	def post(self, id = None):
		try:
			slip_data = json.loads(self.request.body)
		except ValueError:
			self.error_handler(405,'Error: Invalid JSON in request body.')
		if id:
			self.error_handler(403, "Error: Invalid ID value. Should be null.")
		if Slip.query(Slip.number == slip_data['number']).get():
			self.error_handler(403, "Error: Slip already exists.")

		if not self.err:
			slip_data['arrival_date'] = "null";
			slip_data['current_boat'] = "null";
			newSlip = Slip(**slip_data)
			newSlip.put()
			slip_dict = newSlip.to_dict()
			slip_dict['self'] = '/slips/' + id
			self.response.write(json.dumps(slip_dict))

	def get(self, id=None):
		if id:
			try:
				slip = ndb.Key(urlsafe=id).get()
			except ValueError:
				self.error_handler(405,'Error: No such slip by that ID.')
			if not slip:
				self.error_handler(405,"Error: Invalid Slip ID.")
			if not self.err:
				s_d = slip.to_dict()
				s_d['self'] = '/slips/' + id
				self.response.write(json.dumps(s_d))
		else:
			slips = Slip.query().fetch()
			s_ds = {'Slips':[]}
			for slip in slips:
				id = slip.key.urlsafe()
				slip_data=slip.to_dict()
				slip_data['self'] = '/slips/' + id
				slip_data['id'] = id
				s_ds['Slips'].append(slip_data)
			self.response.write(json.dumps(s_ds))

	def delete(self, id = None):

		if not id:
			slips = Slip.query().fetch()
			for slip in slips:
				slip.key.delete()
			return
		if not self.err:
			try:
				slip = ndb.Key(urlsafe=id).get()
			except ValueError:
				self.error_handler(405,'Error: No such slip by that ID.')
			if not slip:
				self.error_handler(405, "Error: can't find slip id.")
		if not self.err:
			if (slip.current_boat != "null"):
				boat = ndb.Key(urlsafe=slip.current_boat).get()
				boat.at_sea = True
				boat.slip = 'null'
				boat.put()
			slip.key.delete()
			self.response.write('Slip Deleted!')

	def patch(self, id = None):
		if not id:
			self.error_handler(403, "Error: No ID provided.")

		if not self.err:
			try:
				body = json.loads(self.request.body)
			except ValueError: 
				self.error_handler(405,'Erorr: No request body.')

		if not self.err:
			try:
				slip = ndb.Key(urlsafe=id).get()
			except ValueError:
				self.error_handler(405,'Error: No such slip by that ID.')
			if 'number' in body:
				slip.number = body['number']
				slip.put()
				self.response.write(json.dumps(body))
















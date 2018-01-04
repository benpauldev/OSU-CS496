from google.appengine.ext import ndb
import webapp2
import json

class HarborMaster(webapp2.RequestHandler):

	def error_handler(self, errorNo, errorMsg):
		self.response.status = errorNo
		self.response.write(errorMsg)
		self.err = True

	def put(self, slip_id):
		
		self.err = False

		try:
			body = json.loads(self.request.body)
		except ValueError:
			self.error_handler(405, 'Error: Invalid JSON in request body.')

		if not self.err:    
			boat = ndb.Key(urlsafe=body['boat_id']).get()
			if not boat:
				self.error_handler(405, 'Error: Bad boat ID.')

		if not self.err:    
			slip = ndb.Key(urlsafe=slip_id).get()
			if not slip:
				self.error_handler(405, 'Error: Bad slip ID.')

		if not self.err:
			if slip.current_boat != 'null':
				self.error_handler(403, 'Error: Slip occupied!')

		if not self.err:
			slip.arrival_date = body['arrival_date']
			slip.current_boat = body['boat_id']
			boat.at_sea = False
			boat.slip = slip_id
			slip.put()
			boat.put()
			slip_dict = slip.to_dict()
			boat_dict = boat.to_dict()
			slip_dict['current_boat'] = '/boats/' + body['boat_id']
			boat_dict['slip'] = '/slips/' + slip_id
			self.response.write(json.dumps(slip_dict))
			self.response.write(json.dumps(boat_dict))
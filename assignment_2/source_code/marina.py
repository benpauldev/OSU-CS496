from google.appengine.ext import ndb
import webapp2
import json
from google.net.proto.ProtocolBuffer import ProtocolBufferDecodeError

from slips import Slip, _slips
from boats import Boat, _boats
from harbor_master import HarborMaster

class MainPage(webapp2.RequestHandler):
	def get(self):
		self.resonse.write('Ahoy, Matey!')

		
allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/boats',_boats),
    ('/boats/(.*)',_boats),
    ('/slips/?(.*)',_slips),
    ('/harbor_master/(.*)',HarborMaster)
], debug=True)
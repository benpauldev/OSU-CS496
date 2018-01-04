from google.appengine.ext import ndb
import webapp2
import json

class Fish(ndb.Model):
	name = ndb.StringProperty(required=True)
	ph_min = ndb.IntegerProperty()
	ph_max = ndb. IntegerProperty()


class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write('Hello, webapp2!')

class FishHandler(webapp2.RequestHandler):
	def post(self):
		parent_key = ndb.Key(Fish, "parent_fish")
		fish_data = json.loads(self.request.body)
		new_fish = Fish(name=fish_data['name'], parent=parent_key)
		new_fish.put()
		fish_dict = new_fish.to_dict()
		fish_dict['self'] = '/fish/' + new_fish.key.urlsafe()
		self.response.write(json.dumps(fish_dict))

	def get(self, id=None):
		if id:
			f = ndb.Key(urlsafe=id).get()
			f_d = f.to_dict()
			f_d['self'] = "/fish/" + id
			self.response.write(json.dumps(f_d))

		

allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/fish',FishHandler),
    ('/fish/(.*)',FishHandler)
], debug=True)
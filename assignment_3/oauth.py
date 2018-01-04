import webapp2
import os
import urllib
import json
import hashlib
import google.appengine.api.urlfetch as urlfetch
from google.appengine.ext.webapp import template


CLIENT_ID = //client id
CLIENT_SECRET = //client secret
GOOGLE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
GPLUS_URL = "https://www.googleapis.com/plus/v1/people/me"
REDIRECT_URI = "https://oauth-app-183500.appspot.com/oauth"
#https://docs.python.org/3/library/hashlib.html
#https://piazza.com/class/ixpxeov152k3n?cid=114
STATE = hashlib.sha256(os.urandom(1024)).hexdigest()

class OauthPage(webapp2.RequestHandler):
    def get(self):
        auth_state = self.request.get('state')
        access_code = self.request.get('code')
        #logging.info(auth_state);


        header = {'Content-Type':'application/x-www-form-urlencoded'}
        queryString ={'code':access_code,
        	    'client_id':CLIENT_ID,
        	    'client_secret':CLIENT_SECRET,
        	    'redirect_uri':REDIRECT_URI,
        	    'grant_type':'authorization_code'}

        encoded_data = urllib.urlencode(queryString)

  
        result = urlfetch.fetch("https://www.googleapis.com/oauth2/v4/token/",
                                headers=header, payload=encoded_data,
                                method=urlfetch.POST)
        token = json.loads(result.content)

  
        header = {'Authorization': 'Bearer ' + token['access_token']}
        response = urlfetch.fetch(GPLUS_URL, headers=header)
        
        google_data = json.loads(response.content)
    
    	
        html_values = {'username': google_data['displayName'],
                         'email': google_data['emails'][0]['value'],
        
                         'state': STATE}
        # webapp2 getting started django templates
        # http://webapp2.readthedocs.io/en/latest/tutorials/gettingstarted/templates.html#next
        path = os.path.join(os.path.dirname('templates/'), 'token.html')
        self.response.out.write(template.render(path, html_values))



class MainPage(webapp2.RequestHandler):
    def get(self):

        login = GOOGLE_URL + '?' + 'response_type=code&' + \
                'client_id=' + CLIENT_ID +'&' + \
                'redirect_uri=' + REDIRECT_URI + '&' + \
                'scope=email&' + 'state=' + STATE
        

     
        html_values = {"login": login}

        path = os.path.join(os.path.dirname('templates/'), 'index.html')
        self.response.out.write(template.render(path, html_values))
        return


app = webapp2.WSGIApplication([('/', MainPage),
                               ('/index.html', MainPage),
                               ('/oauth', OauthPage)],
                              debug=True)

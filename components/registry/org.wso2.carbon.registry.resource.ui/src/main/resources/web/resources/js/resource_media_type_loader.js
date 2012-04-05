/*load media types function is defined in the resource_util.js
  since resource_util.js is used even the user is not login, by placing
  the following function inside the resource_util.js gives 
  user not authorized errors. so we are seperating the media type loader
  to a seperate js file.*/
loadMediaTypes();

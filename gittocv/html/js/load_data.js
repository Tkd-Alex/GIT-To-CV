for(var i in data){
	var card = document.createElement('div');
	card.className = "col-md-2";
	if(data[i].avatar_url == null || data[i].avatar_url.trim() == "") data[i].avatar_url = "img/github_profile.png";

	commit_star = "";
	for(var star=1; star<=5; star++)
		commit_star += '<span class=" ' + ( star<=data[i].commit_star ? "glyphicon glyphicon-star" : "glyphicon glyphicon-star-empty" ) + ' "></span>'
	
	card.innerHTML = '\
		<div class="panel panel-default">\
    		<div class="panel-body">\
    		<p><img src="' + data[i].avatar_url + '" class="img-thumbnail" style="width: 300px;"></p>\
          	<div class="text-left">\
          		<h1 class="page-header">' + data[i].name + '<br/ ><small>' + (( data[i].username == null || data[i].username.trim() == "" ) ? "&nbsp;" : data[i].username) + '</small></h1>\
            	\
            	<div><h3> Commits: ' + data[i].commit + "&nbsp;&nbsp;&nbsp;&nbsp;" + commit_star + '</h3></div>\
				<div class="progress">\
					<div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="' + data[i].writer + '" style="width:' + data[i].writer + '%" >\
						' + data[i].writer + '% Writer\
					</div>\
				</div>\
				<div class="progress">\
					<div class="progress-bar progress-bar-info" role="progressbar" aria-valuenow="' + data[i].backend + '" style="width:' + data[i].backend + '%" >\
						' + data[i].backend + '% Backed\
					</div>\
				</div>\
				<div class="progress">\
					<div class="progress-bar progress-bar-warning" role="progressbar" aria-valuenow="' + data[i].frontend + '" style="width:' + data[i].frontend + '%" >\
						' + data[i].frontend + '% Frontend\
					</div>\
				</div>\
				\
           		<div style="overflow-y: auto; height: 100px;"><p>' + data[i].bio + '<p></div><br />\
            	<p><span class="glyphicon glyphicon-envelope" aria-hidden="true"></span> ' + data[i].email + '<p>\
            	<p><span class="glyphicon glyphicon-map-marker" aria-hidden="true"></span> ' + data[i].location + '<p>\
            	<p><span class="glyphicon glyphicon-link" aria-hidden="true"></span> ' + data[i].website + '<p>\
            </div>\
            </div>\
      	</div>';
	document.getElementById('main-row').appendChild(card);
}
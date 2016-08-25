var registerCtrlr = function($scope, $http, Upload){
	
	var today = new Date();
	
	$scope.user = {'identifications':{}, 'marketing':{}};
	$scope.reco = {year:new Date().getFullYear()};
	$scope.dob = {};
	$scope.today = today.getFullYear() + "-" + (today.getMonth() + 1) + "-" + today.getDate();
		
	
	$scope.hearAboutOptions = ['WeedMaps', 'Yelp', 'Facebook', 'Leafly', 'Friends & Family', 'La Weekly', 'Other'];
	
	$scope.register = function(){
		
		var proceed = true;
		$scope.pageLevelAlert = "";
		$('.recofile-group, .idfile-group, .dobWrapper, .recoWrapper').removeClass('has-error');		

		
		if($scope.invalidDob()){
			proceed = false;
			$('.dobWrapper').addClass('has-error');
		}
		
		if($scope.invalidReco()){
			proceed = false;
			$('.recoWrapper').addClass('has-error');
		}
		
		if($scope.registerForm.$valid 
				&& $scope.recoFile 
				&& $scope.idFile
				&& proceed){
			
			$http.post(_lbUrls.register, $scope.user)
			.then(function(response){
				var resp = response.data;	
				if(resp.success){
					
					//Remove files from localStorage
					localStorage.removeItem('recoFile');
					localStorage.removeItem('idFile');
					
					location.href = "/pending-registration";
				}
				else{
					$scope.pageLevelAlert = resp.message; 
				}
			},
			
			function(){
				$scope.pageLevelAlert = "There was some error creating your account. "
					+"Please try later. If problem persists, please call the customer care.";
			});	
		}
		
		else{
			
			if(!$scope.recoFile){
				$('.recofile-group').addClass('has-error');
			}
			
			if(!$scope.idFile){
				$('.idfile-group').addClass('has-error');
			}
			
			if(!$scope.registerForm.$valid){
				$scope.pageLevelError = 'Please correct the highlighted sections'; 
			}
			
			$('html,body').scrollTop($('.has-error').offset().top-120);
		}
	};
	
	
	$scope.removeFile = function(type){
		
		if(type == 'id' && $scope.idFile){			
			$http.post('/files/delete/id/' + $scope.idFile._id);
			$scope.idFile = null;
			localStorage.removeItem('idFile');
		}
		
		else if(type == 'reco' && $scope.recoFile){
			$http.post('/files/delete/id/' + $scope.recoFile._id);
			$scope.recoFile = null;
			localStorage.removeItem('recoFile');
		}
	};
	
	$scope.invalidFname = function(){		
		return $scope.registerForm.firstname.$invalid 
			&& !$scope.registerForm.firstname.$pristine;
	};
	
	$scope.invalidLname = function(){		
		return $scope.registerForm.lastname.$invalid 
			&& !$scope.registerForm.lastname.$pristine;
	};
	
	$scope.invalidEmail = function(){		
		return $scope.registerForm.email.$invalid 
			&& !$scope.registerForm.email.$pristine;
	};
	
	$scope.invalidUname = function(){		
		return $scope.registerForm.username.$invalid 
			&& !$scope.registerForm.username.$pristine;
	};
	
	$scope.invalidPassword = function(){		
		return $scope.registerForm.password.$invalid 
			&& !$scope.registerForm.password.$pristine;
	};
	
	$scope.invalidCPassword = function(){		
		return !$scope.registerForm.confirmpassword.$pristine
			&& ($scope.user.password != $scope.confirmPassword);
	};
	
	$scope.invalidPhone = function(){		
		return $scope.registerForm.phone.$invalid 
			&& !$scope.registerForm.phone.$pristine;
	};
	
	$scope.invalidDob = function(){	
		
		if($scope.registerForm.dobDay.$valid 
				&& $scope.registerForm.dobMonth.$valid 
				&& $scope.registerForm.dobYear.$valid){
			
			var now = new Date(),
			currYear = now.getFullYear(),
			year21Before = currYear - 21;
			
			//Set years back by 21
			now.setFullYear(year21Before);
			
			$scope.user.dob = new Date($scope.dob.year, $scope.dob.month-1, $scope.dob.day);
			
			//if user dob is greater than year21Before, then user is below 21 
			if($scope.user.dob.getTime() > now.getTime()){
				return true;
			}
		}
		
		return ($scope.registerForm.dobDay.$invalid && !$scope.registerForm.dobDay.$pristine) 
			|| ($scope.registerForm.dobMonth.$invalid && !$scope.registerForm.dobMonth.$pristine) 
			|| ($scope.registerForm.dobYear.$invalid && !$scope.registerForm.dobYear.$pristine);
	};
	
	$scope.invalidReco = function(){
		
		if($scope.registerForm.recoDay.$valid 
				&& $scope.registerForm.recoMonth.$valid 
				&& $scope.registerForm.recoYear.$valid){
		
			var now = new Date();
			$scope.user.identifications.recoExpiry = new Date($scope.reco.year, $scope.reco.month-1, $scope.reco.day);
			
			if($scope.user.identifications.recoExpiry.getTime() < now.getTime()){
				return true;
			}
		}
		
		return ($scope.registerForm.recoDay.$invalid && !$scope.registerForm.recoDay.$pristine) 
			|| ($scope.registerForm.recoMonth.$invalid && !$scope.registerForm.recoMonth.$pristine) 
			|| ($scope.registerForm.recoYear.$invalid && !$scope.registerForm.recoYear.$pristine);
	};

	
    $scope.$watch('idfileobj', function () {
        new $scope.upload($scope.idfileobj, 'id');
    });

	
    $scope.$watch('recofileobj', function () {
        new $scope.upload($scope.recofileobj,'reco');
    });
    
    
    $scope.upload = function (file, type) {
      if (file) {
	        var fileInfo = {progress : false, element : null};
	        
	        Upload.upload({
	            url: '/files/upload',
	            fields : {path : '/user/'+$scope.today + "/", ctrl : 'controlled'},
	        	file: file,
	        	fileInfo : fileInfo
	            
	        }).then(
	
	    	        function (resp) { //Success
		    	        
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.closest('.row').remove();
			
			           if(resp.data.results){
			        	   var img = resp.data.results[0];
			        	   
			        	   if(img){
				        	   if(type && type=='id'){
				        		   $scope.user.identifications.idCard = img.location;
				        		   $scope.idFile = img;
				        		   localStorage.setItem('idFile', JSON.stringify(img));
				        	   }
				        	   else if(type && type=='reco'){
				        		   $scope.user.identifications.recomendation = img.location;
				        		   $scope.recoFile = img;
				        		   localStorage.setItem('recoFile', JSON.stringify(img));
				        	   }
			        	   }
			           }
			            
			        },
	
	
	
	    	        function (resp) {//Error
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.parent().html('<div class="progress-bar progress-bar-danger"style="width: 100%">Error Uploading File</div>');
			            
			            console.log('Error uploading the file');
			        },
	
	
	    	        function (evt) { //Progress
			        	if(!evt.config.fileInfo.progress){
				        	var f = evt.config.fileInfo;
				        	
				        	var container = '.progress-wrapper ';
				        	if(type) container = '.' + type + '-progress-wrapper';
				        	
			        		f.progress = true;
			        		f.element = $('<div />').attr({'class': 'progress-bar progress-bar-success', 'role':'progressbar','style':'width:0%'});
			        		
			        		$('<div class="row"><div class="col-xs-4 file-name-holder">' 
			                		+ evt.config.file.name 
			                		+ '</div><div class="col-xs-8"><div class="progress"></div></div>')
			                		.appendTo(container)
			                		.find('.progress')
			                		.append(f.element);
			        	}
	
			            if(evt.config.fileInfo){
			            	var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
			            	evt.config.fileInfo.element.css('width', progressPercentage+'%').html(progressPercentage+'%');
			            }
			        }
			 );
      }
    };
    
    $scope.init = function(){
    	
    	try {
    		
        	if(localStorage.getItem('recoFile')){
        		$scope.recoFile = JSON.parse(localStorage.getItem('recoFile')); 
      		   	$scope.user.identifications.recomendation = $scope.recoFile.location;
     	   	}
        	
        	if(localStorage.getItem('idFile')){
        		$scope.idFile = JSON.parse(localStorage.getItem('idFile'));
        		$scope.user.identifications.idCard = $scope.idFile.location;
        	}
    	}catch(e){}
    };
    
    $scope.init();
};

upstream api_server {
    server    127.0.0.1:8080;
}

server {

	root /var/www/apps/volleyball-referee.com/dist;

	# Add index.php to the list if you are using PHP
	index index.html index.htm index.nginx-debian.html;
    server_name www.volleyball-referee.com; # managed by Certbot


	location / {
		try_files $uri $uri/ /index.html;
		autoindex on;
    		autoindex_exact_size off;
	}

	location /api {
        	proxy_set_header X-Real-IP $remote_addr;
	        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        	proxy_set_header Host $http_host;
        	proxy_set_header X-NginX-Proxy true;
        	#rewrite ^/api/?(.*) /$1 break;
 
        	proxy_pass http://api_server;
        	proxy_redirect off;

					location /api/v3.2/public/ {
							limit_req zone=public;
							proxy_set_header X-Real-IP $remote_addr;
							proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
							proxy_set_header Host $http_host;
							proxy_set_header X-NginX-Proxy true;
							#rewrite ^/api/?(.*) /$1 break;
		
							proxy_pass http://api_server;
							proxy_redirect off;
					}

					location /api/v3.2/pro/ {
							limit_req zone=pro;
							proxy_set_header X-Real-IP $remote_addr;
							proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
							proxy_set_header Host $http_host;
							proxy_set_header X-NginX-Proxy true;
							#rewrite ^/api/?(.*) /$1 break;
		
							proxy_pass http://api_server;
							proxy_redirect off;
					}
	}


    listen [::]:443 ssl ipv6only=on; # managed by Certbot
    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/www.volleyball-referee.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/www.volleyball-referee.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
    keepalive_timeout 70;

}
server {
    if ($host = www.volleyball-referee.com) {
        return 301 https://$host$request_uri;
    } # managed by Certbot

    if ($host = volleyball-referee.com) {
        return 301 https://$host$request_uri;
    } # managed by Certbot
	      
	listen 80 ;
	listen [::]:80 ;
    server_name www.volleyball-referee.com;
    return 404; # managed by Certbot


}

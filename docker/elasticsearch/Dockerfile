FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0
COPY analysis-ik-8.11.0.zip /tmp/analysis-ik.zip
RUN bin/elasticsearch-plugin install --batch file:///tmp/analysis-ik.zip

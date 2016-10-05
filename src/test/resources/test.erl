-module(test).
-behaviour(application).
-export([start/2, stop/1]).

start(_Type, _Args) ->
  io:fwrite("Hello world!~n", []). 

stop(_) ->
  ok.

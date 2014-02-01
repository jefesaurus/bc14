#!/bin/bash
cp bca.conf bc.conf
ant file > out.txt
grep -c 'team169 (A) wins' out.txt
grep -c 'wins' out.txt
cp bcb.conf bc.conf
ant file > out.txt
grep -c 'team169 (B) wins' out.txt
grep -c 'wins' out.txt

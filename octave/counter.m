function data = counter(titel)
co = dir(titel);
name = [];
iarr = [];
marr = [];
inte = [];
hal  = [];
rot =1;
max1 = 0;
c= 1;
figure(1);
hold on;

for i=1:length(co)
 name = co(i).name;
 printf("Loading Data: %s\n",name);
 %iarr = [iarr;c];
 %c++;
 hplData = csvread(co(i).name);
 hplp1 = [1;0];
 hplp2 = [0;1];
 hplx = hplData * hplp1;
 hply = hplData * hplp2;
 subplot(2,1,1);
 hold on;
 plot(hplx,hply,'Color',[rot/10 1-rot/20 rot/20], sprintf('%d;;',i));
 %plot(hplx,hply,sprintf('%d;%s;',i-1,co(i).name));
 %rot++;
 %s1 = start(hply);
 %f1= leftit(hplx,hply,s1);
 %subplot(1,2,1);
 %hold on;
 %plot(f1(:,1),f1(:,2));

 
end

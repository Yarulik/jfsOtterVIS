function data = plot_csv(titel)
co = dir(titel);
name = [];

figure(1);
hold on;

for i=1:length(co)
 name = co(i).name;
 printf("Loading Data: %s\n",name);
 hplData = csvread(co(i).name);
 hplp1 = [1;0];
 hplp2 = [0;1];
 hplx = hplData * hplp1;
 hply = hplData * hplp2;
 subplot(2,1,1);
 plot(hplx,hply,'Color',[rand(1) rand(1) rand(1)],sprintf(';%d;',i));
 hold on;
endfor
h = legend("show");

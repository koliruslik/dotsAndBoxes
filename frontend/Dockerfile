FROM node:20-alpine

WORKDIR /app

RUN npm install -g serve

COPY package.json ./

COPY . .

# Expose port
EXPOSE 3000

CMD ["serve", "-s", ".", "-l", "3000"]

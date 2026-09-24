# Base image Node.js LTS (node 22 for native WebSocket support)
FROM node:22-alpine

# Set working directory
WORKDIR /app

# Copy package.json & package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install --production

# Copy source code
COPY . .

# Expose port 3000
EXPOSE 3000

# Set default ENV
ENV PORT=3000

# Run backend
CMD ["npm", "start"]

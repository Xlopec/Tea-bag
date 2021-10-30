import SwiftUI
import SharedAppLib
import Kingfisher
import SharedAppLib
import CoreMIDI
import CoreAudio

struct ArticlesView: View {
    
    let state: ArticlesState
    let handler: MessageHandler
    
    @State
    private var searchText: String
    
    init(state: ArticlesState, handler: @escaping MessageHandler) {
        self.state = state
        self.handler = handler
        self.searchText = state.query.input
    }
    
    var body: some View {
        ZStack {
            
            TabView {
                
                VStack(alignment: .leading, spacing: 10) {
                    
                    Text("Feed")
                        .font(.title)
                        .padding()
                    
                    SearchBar(text: $searchText, hintText: "Search in articles...") {
                        handler(LoadArticlesFromScratch(id: state.id))
                    }.onChange(of: searchText) { updatedSearchText in
                        // todo send request to fetch and show suggestions
                        handler(OnQueryUpdated(id: state.id, query: updatedSearchText))
                    }
                    
                    if state.isLoading {
                        ZStack {
                            ProgressView()
                        }
                        .frame(
                            maxWidth: .infinity,
                            maxHeight: .infinity,
                            alignment: .center
                        )
                    } else if let transientState = state.transientState as? ArticlesState.TransientStateException, state.articles.isEmpty {
                        MessageView(message: transientState.displayMessage, actionButtonMessage: "Retry") {
                            handler(RefreshArticles(id: state.id))
                        }.frame(
                            maxWidth: .infinity,
                            maxHeight: .infinity,
                            alignment: .center
                        )
                    } else {
                        
                        List {
                            
                            ForEach(state.articles, id: \.url) { article in
                                RowItem(screenId: state.id, article: article, handler: handler)
                                    .onAppear {
                                        // give window of size of 2 last items in order to prefetch next articles
                                        // before user will scroll to the end of the list
                                        if article == state.articles[safe: state.articles.count - 2] {
                                            handler(LoadNextArticles(id: state.id))
                                        }
                                    }
                            }
                            
                            VStack(alignment: .center) {
                                if state.isLoadingNext {
                                    ProgressView()
                                } else if let transientState = state.transientState as? ArticlesState.TransientStateException {
                                    MessageView(message: transientState.displayMessage, actionButtonMessage: "Retry") {
                                        handler(RefreshArticles(id: state.id))
                                    }
                                }
                            }.frame(maxWidth: .infinity)
                        }.refreshable {
                            handler(RefreshArticles(id: state.id))
                        }
                    }
                    
                }
                
                .tabItem {
                    Image(systemName: "globe")
                    Text("Articles")
                }
                
                Text("pidor")
                    .tabItem {
                        Image(systemName: "heart")
                        Text("Favorites")
                    }
                
                Text("suka")
                    .tabItem {
                        Image(systemName: "chart.line.uptrend.xyaxis")
                        Text("Trending")
                    }
                
                Text("blyat")
                    .tabItem {
                        Image(systemName: "gear")
                        Text("Settings")
                    }
                
            }
            .font(.headline)
        }
    }
}

struct RowItem: View {
    
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd MMM' at 'hh:mm"
        return formatter
    }()
    
    let article: Article
    let handler: MessageHandler
    let screenId: UUID
    
    // todo how to pass environmental vars implicitly?
    init(screenId: UUID, article: Article, handler: @escaping MessageHandler) {
        self.article = article
        self.handler = handler
        self.screenId = screenId
    }
    
    var body: some View {
        
        VStack(alignment: .leading, spacing: 10, content: {
            
            if let image = article.urlToImage {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(height: 200, alignment: .center)
            }
            // todo get rid of casts
            Text(article.title as! String)
            
            if let author = article.author as? String {
                Text("By \(author)")
                    .font(.caption)
            }
            
            if let description = article.description_ as? String {
                Text(description)
                    .font(.subheadline)
                    .lineLimit(100)
            }
            
            Text("Published on \(RowItem.dateFormatter.string(from: article.published))")
                .font(.caption)
        }).background {
            // Context menu seems keeping old state and doesn't update on state change
            // see: https://stackoverflow.com/questions/68313742/why-does-context-menu-display-the-old-state-even-though-the-list-has-correctly-b
            Color
                .clear
                .contextMenu {
                    Button {
                        showSharePopup()
                        // handler(ShareArticle(article: article))
                    } label: {
                        Label("Share", systemImage: "square.and.arrow.up")
                    }
                    
                    Button {
                        handler(ToggleArticleIsFavorite(id: screenId, article: article))
                    } label: {
                        Label(article.isFavorite ? "Remove from favorite" : "Add to favorite", systemImage: article.isFavorite ? "heart.fill" : "heart")
                    }
                }.id(article.isFavorite)
        }
        
        /*.padding(
         EdgeInsets(
         top: CGFloat(16.0),
         leading: CGFloat(16.0),
         bottom: CGFloat(16.0),
         trailing: CGFloat(16.0)
         )
         )*/
    }
    
    private func showSharePopup() {
        // todo rework with AppResolver in mind
        let urlShare = article.url
        let activityVC = UIActivityViewController(activityItems: [urlShare], applicationActivities: nil)
        UIApplication.shared.windows.first?.rootViewController?.present(activityVC, animated: true, completion: nil)
    }
}

struct ContentView_Previews: PreviewProvider {
    
    static private let article = Article(url: URL(string: "www.google.com")!, title: "Title", author: nil, description: nil, urlToImage: nil, published: Date(), isFavorite: true)
    
    static var previews: some View {
        RowItem(screenId: UUID(), article: article) {_ in }
        
        ArticlesView(state: ArticlesState(id: UUID(), query: Query(input: "Ios articles", type: QueryType.favorite), articles: [article], hasMoreArticles: true, transientState: ArticlesState.TransientStatePreview.shared))  { _ in }
    }
}

private extension ArticlesState.TransientStateException {
    var displayMessage: String { return th.message ?? "Failed to load articles, please, try again later" }
}

private extension Collection {
    
    subscript (safe index: Index) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
    
}
